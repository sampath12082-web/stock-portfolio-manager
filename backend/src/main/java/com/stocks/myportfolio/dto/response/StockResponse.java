package com.stocks.myportfolio.dto.response;

import com.stocks.myportfolio.common.enums.Exchange;

public record StockResponse(
        Long id,
        String symbol,
        String companyName,
        Exchange exchange,
        String sector,
        String industry
) {
}