package com.stocks.myportfolio.dto.request.mf;

import jakarta.validation.constraints.NotBlank;

public record CreateMfRequest(
        @NotBlank String schemeCode,
        @NotBlank String schemeName,
        String fundHouse,
        String isin,
        String category,
        String fundType) {
}
