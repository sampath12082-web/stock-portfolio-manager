package com.stocks.myportfolio.dto.response.mf;

import java.math.BigDecimal;

public record MfLookupResponse(
        String schemeCode,
        String schemeName,
        BigDecimal nav,
        String navDate,
        boolean existsInDb) {
}
