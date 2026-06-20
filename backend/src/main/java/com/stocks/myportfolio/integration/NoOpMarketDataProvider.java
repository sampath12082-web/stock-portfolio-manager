package com.stocks.myportfolio.integration;

import java.util.List;
import java.util.Map;

import com.stocks.myportfolio.common.enums.Exchange;
import com.stocks.myportfolio.common.exception.MarketDataException;

public class NoOpMarketDataProvider implements MarketDataProvider {

    @Override
    public StockQuoteData fetchQuote(String symbol, Exchange exchange) {
        throw new MarketDataException(
                "Market data provider not configured.");
    }

    @Override
    public Map<String, StockQuoteData> fetchQuotes(List<String> symbols, Exchange exchange) {
        throw new MarketDataException(
                "Market data provider not configured.");
    }
}
