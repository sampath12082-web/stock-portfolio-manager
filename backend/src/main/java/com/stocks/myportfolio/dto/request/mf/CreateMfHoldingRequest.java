package com.stocks.myportfolio.dto.request.mf;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMfHoldingRequest(
        @NotBlank String schemeCode,
        @NotNull BigDecimal units,
        @NotNull BigDecimal averageNav) {
}
