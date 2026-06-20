package com.stocks.myportfolio.dto.request.mf;

import java.math.BigDecimal;

import com.stocks.myportfolio.common.enums.MfTransactionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMfTransactionRequest(
        @NotBlank String schemeCode,
        @NotNull BigDecimal units,
        @NotNull BigDecimal nav,
        @NotNull BigDecimal amount,
        @NotNull MfTransactionType transactionType,
        String description,
        String tradeDate,
        String folioNumber) {
}
