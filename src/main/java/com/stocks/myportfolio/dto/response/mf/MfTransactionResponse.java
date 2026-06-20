package com.stocks.myportfolio.dto.response.mf;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.stocks.myportfolio.common.enums.MfTransactionType;

public record MfTransactionResponse(
        Long id,
        String schemeCode,
        String schemeName,
        BigDecimal units,
        BigDecimal nav,
        BigDecimal amount,
        MfTransactionType transactionType,
        String description,
        String folioNumber,
        LocalDateTime tradeDate,
        LocalDateTime createdAt) {
}
