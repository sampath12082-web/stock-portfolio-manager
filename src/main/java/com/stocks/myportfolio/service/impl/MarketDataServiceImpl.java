package com.stocks.myportfolio.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.entity.StockQuote;
import com.stocks.myportfolio.integration.MarketDataProvider;
import com.stocks.myportfolio.integration.StockQuoteData;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.repository.StockQuoteRepository;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.service.MarketDataService;

@Service
public class MarketDataServiceImpl implements MarketDataService {

    private static final int CACHE_MINUTES = 5;

    private final MarketDataProvider marketDataProvider;
    private final StockQuoteRepository stockQuoteRepository;
    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;

    public MarketDataServiceImpl(
            MarketDataProvider marketDataProvider,
            StockQuoteRepository stockQuoteRepository,
            StockRepository stockRepository,
            HoldingRepository holdingRepository) {

        this.marketDataProvider = marketDataProvider;
        this.stockQuoteRepository = stockQuoteRepository;
        this.stockRepository = stockRepository;
        this.holdingRepository = holdingRepository;
    }

    @Override
    public StockQuoteData getCurrentPrice(String symbol) {
        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock not found: " + symbol));

        Optional<StockQuote> cached = stockQuoteRepository
                .findTopByStockOrderByFetchedAtDesc(stock);

        if (cached.isPresent() && isFresh(cached.get())) {
            return toQuoteData(cached.get());
        }

        StockQuoteData fresh = marketDataProvider.fetchQuote(
                stock.getSymbol(), stock.getExchange());
        saveQuote(stock, fresh);
        return fresh;
    }

    @Override
    public Map<String, StockQuoteData> getCurrentPrices() {
        List<Holding> holdings = holdingRepository.findAll();
        Map<String, StockQuoteData> result = new HashMap<>();

        for (Holding holding : holdings) {
            Stock stock = holding.getStock();
            Optional<StockQuote> cached = stockQuoteRepository
                    .findTopByStockOrderByFetchedAtDesc(stock);

            if (cached.isPresent() && isFresh(cached.get())) {
                result.put(stock.getSymbol(), toQuoteData(cached.get()));
            }
        }

        List<String> staleSymbols = holdings.stream()
                .map(h -> h.getStock().getSymbol())
                .filter(s -> !result.containsKey(s))
                .collect(Collectors.toList());

        if (!staleSymbols.isEmpty()) {
            Map<com.stocks.myportfolio.common.enums.Exchange, List<Holding>> byExchange =
                    holdings.stream()
                            .filter(h -> staleSymbols.contains(h.getStock().getSymbol()))
                            .collect(Collectors.groupingBy(h -> h.getStock().getExchange()));

            for (var entry : byExchange.entrySet()) {
                List<String> symbols = entry.getValue().stream()
                        .map(h -> h.getStock().getSymbol())
                        .collect(Collectors.toList());

                Map<String, StockQuoteData> fetched =
                        marketDataProvider.fetchQuotes(symbols, entry.getKey());

                for (var quoteEntry : fetched.entrySet()) {
                    Stock stock = stockRepository
                            .findBySymbol(quoteEntry.getKey()).orElse(null);
                    if (stock != null) {
                        saveQuote(stock, quoteEntry.getValue());
                    }
                    result.put(quoteEntry.getKey(), quoteEntry.getValue());
                }
            }
        }

        return result;
    }

    @Override
    public void refreshAllQuotes() {
        List<Holding> holdings = holdingRepository.findAll();

        Map<com.stocks.myportfolio.common.enums.Exchange, List<Holding>> byExchange =
                holdings.stream()
                        .collect(Collectors.groupingBy(h -> h.getStock().getExchange()));

        for (var entry : byExchange.entrySet()) {
            List<String> symbols = entry.getValue().stream()
                    .map(h -> h.getStock().getSymbol())
                    .collect(Collectors.toList());

            Map<String, StockQuoteData> fetched =
                    marketDataProvider.fetchQuotes(symbols, entry.getKey());

            for (var quoteEntry : fetched.entrySet()) {
                Stock stock = stockRepository
                        .findBySymbol(quoteEntry.getKey()).orElse(null);
                if (stock != null) {
                    saveQuote(stock, quoteEntry.getValue());
                }
            }
        }
    }

    private boolean isFresh(StockQuote quote) {
        return quote.getFetchedAt()
                .isAfter(LocalDateTime.now().minusMinutes(CACHE_MINUTES));
    }

    private void saveQuote(Stock stock, StockQuoteData data) {
        StockQuote quote = new StockQuote();
        quote.setStock(stock);
        quote.setLastTradedPrice(data.ltp());
        quote.setOpenPrice(data.open());
        quote.setHighPrice(data.high());
        quote.setLowPrice(data.low());
        quote.setClosePrice(data.close());
        quote.setPreviousClose(data.previousClose());
        quote.setVolume(data.volume());
        quote.setFetchedAt(data.fetchedAt());
        stockQuoteRepository.save(quote);
    }

    private StockQuoteData toQuoteData(StockQuote quote) {
        return new StockQuoteData(
                quote.getStock().getSymbol(),
                quote.getLastTradedPrice(),
                quote.getOpenPrice(),
                quote.getHighPrice(),
                quote.getLowPrice(),
                quote.getClosePrice(),
                quote.getPreviousClose(),
                quote.getVolume(),
                quote.getFetchedAt());
    }
}
