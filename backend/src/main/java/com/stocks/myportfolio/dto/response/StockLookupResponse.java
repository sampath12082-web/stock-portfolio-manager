package com.stocks.myportfolio.dto.response;

public record StockLookupResponse(
        String symbol,
        String companyName,
        String exchange,
        String sector,
        String industry,
        boolean existsInDb) {
}
