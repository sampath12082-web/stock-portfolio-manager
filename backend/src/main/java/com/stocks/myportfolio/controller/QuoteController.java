package com.stocks.myportfolio.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.integration.StockQuoteData;
import com.stocks.myportfolio.service.MarketDataService;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final MarketDataService marketDataService;

    public QuoteController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/{symbol}")
    public StockQuoteData getQuote(@PathVariable String symbol) {
        return marketDataService.getCurrentPrice(symbol);
    }

    @GetMapping
    public Map<String, StockQuoteData> getAllQuotes() {
        return marketDataService.getCurrentPrices();
    }

    @PostMapping("/refresh")
    public void refreshQuotes() {
        marketDataService.refreshAllQuotes();
    }
}
