package com.stocks.myportfolio.dto.response.mf;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MfFundResponse(
        Long id,
        String schemeCode,
        String schemeName,
        String fundHouse,
        String isin,
        String category,
        String fundType,
        BigDecimal currentNav,
        LocalDate navDate) {
}
