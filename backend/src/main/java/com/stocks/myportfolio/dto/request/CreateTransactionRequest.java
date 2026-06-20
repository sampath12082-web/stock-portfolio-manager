package com.stocks.myportfolio.dto.request;

import java.math.BigDecimal;

import com.stocks.myportfolio.common.enums.TransactionType;

public record CreateTransactionRequest(
    String symbol,
    Integer quantity,
    BigDecimal price,
    TransactionType transactionType,
    String description,
    String tradeDate,
    String tradeType
) {
}
