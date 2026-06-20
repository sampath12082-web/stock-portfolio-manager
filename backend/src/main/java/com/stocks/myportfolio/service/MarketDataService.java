package com.stocks.myportfolio.service;

import java.util.Map;

import com.stocks.myportfolio.integration.StockQuoteData;

public interface MarketDataService {

    StockQuoteData getCurrentPrice(String symbol);

    Map<String, StockQuoteData> getCurrentPrices();

    void refreshAllQuotes();
}
