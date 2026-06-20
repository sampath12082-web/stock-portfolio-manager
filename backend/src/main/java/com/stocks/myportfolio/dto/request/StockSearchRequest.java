package com.stocks.myportfolio.dto.request;

import com.stocks.myportfolio.common.enums.Exchange;

public record StockSearchRequest(
        String query,
        Exchange exchange,
        String sector) {
}
