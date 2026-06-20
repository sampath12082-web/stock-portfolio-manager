package com.stocks.myportfolio.integration;

import java.util.List;
import java.util.Map;

import com.stocks.myportfolio.common.enums.Exchange;

public interface MarketDataProvider {

    StockQuoteData fetchQuote(String symbol, Exchange exchange);

    Map<String, StockQuoteData> fetchQuotes(List<String> symbols, Exchange exchange);
}
