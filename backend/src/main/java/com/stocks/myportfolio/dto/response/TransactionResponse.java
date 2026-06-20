package com.stocks.myportfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.stocks.myportfolio.common.enums.TradeType;
import com.stocks.myportfolio.common.enums.TransactionType;

public record TransactionResponse(
        Long id,
        String symbol,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalAmount,
        TransactionType transactionType,
        String description,
        LocalDateTime tradeDate,
        LocalDateTime createdAt,
        TradeType tradeType) {
}
