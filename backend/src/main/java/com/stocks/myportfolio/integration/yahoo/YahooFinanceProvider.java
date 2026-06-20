package com.stocks.myportfolio.integration.yahoo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import com.stocks.myportfolio.common.enums.Exchange;
import com.stocks.myportfolio.common.exception.MarketDataException;
import com.stocks.myportfolio.integration.MarketDataProvider;
import com.stocks.myportfolio.integration.StockQuoteData;

@Component
@Primary
public class YahooFinanceProvider implements MarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceProvider.class);

    private final RestClient restClient;

    public YahooFinanceProvider() {
        this.restClient = RestClient.builder()
                .baseUrl("https://query1.finance.yahoo.com")
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    public StockQuoteData fetchQuote(String symbol, Exchange exchange) {
        String yahooSymbol = toYahooSymbol(symbol, exchange);
        try {
            JsonNode root = restClient.get()
                    .uri("/v8/finance/chart/{symbol}?interval=1d&range=1d", yahooSymbol)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode meta = root.path("chart").path("result").path(0).path("meta");
            if (meta.isMissingNode()) {
                throw new MarketDataException("No data returned for " + symbol);
            }

            return new StockQuoteData(
                    symbol,
                    toBigDecimal(meta, "regularMarketPrice"),
                    toBigDecimal(meta, "regularMarketPrice"),
                    toBigDecimal(meta, "regularMarketDayHigh"),
                    toBigDecimal(meta, "regularMarketDayLow"),
                    toBigDecimal(meta, "regularMarketPrice"),
                    toBigDecimal(meta, "chartPreviousClose"),
                    meta.path("regularMarketVolume").asLong(0),
                    LocalDateTime.now());
        } catch (MarketDataException e) {
            throw e;
        } catch (Exception e) {
            throw new MarketDataException(
                    "Failed to fetch quote for " + symbol + " from Yahoo Finance", e);
        }
    }

    @Override
    public Map<String, StockQuoteData> fetchQuotes(List<String> symbols, Exchange exchange) {
        Map<String, StockQuoteData> result = new HashMap<>();
        for (String symbol : symbols) {
            try {
                result.put(symbol, fetchQuote(symbol, exchange));
            } catch (Exception e) {
                log.warn("Failed to fetch quote for {}: {}", symbol, e.getMessage());
            }
        }
        return result;
    }

    public List<HistoricalQuote> fetchHistoricalData(String symbol, Exchange exchange) {
        String yahooSymbol = toYahooSymbol(symbol, exchange);
        try {
            JsonNode root = restClient.get()
                    .uri("/v8/finance/chart/{symbol}?interval=1d&range=3mo", yahooSymbol)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode result = root.path("chart").path("result").path(0);
            JsonNode timestamps = result.path("timestamp");
            JsonNode indicators = result.path("indicators").path("quote").path(0);

            if (timestamps.isMissingNode() || timestamps.isEmpty()) {
                return List.of();
            }

            List<HistoricalQuote> quotes = new ArrayList<>();
            for (int i = 0; i < timestamps.size(); i++) {
                long ts = timestamps.get(i).asLong();
                LocalDate date = Instant.ofEpochSecond(ts)
                        .atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();

                BigDecimal open = toBigDecimal(indicators.path("open"), i);
                BigDecimal high = toBigDecimal(indicators.path("high"), i);
                BigDecimal low = toBigDecimal(indicators.path("low"), i);
                BigDecimal close = toBigDecimal(indicators.path("close"), i);
                long volume = indicators.path("volume").path(i).asLong(0);

                if (close != null) {
                    quotes.add(new HistoricalQuote(date, open, high, low, close, volume));
                }
            }
            return quotes;
        } catch (Exception e) {
            log.warn("Failed to fetch historical data for {}: {}", symbol, e.getMessage());
            return List.of();
        }
    }

    private BigDecimal toBigDecimal(JsonNode arrayNode, int index) {
        JsonNode val = arrayNode.path(index);
        if (val.isMissingNode() || val.isNull()) return null;
        return BigDecimal.valueOf(val.asDouble());
    }

    private String toYahooSymbol(String symbol, Exchange exchange) {
        if (exchange == Exchange.BSE) {
            return symbol + ".BO";
        }
        return symbol + ".NS";
    }

    private BigDecimal toBigDecimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return BigDecimal.valueOf(value.asDouble());
    }
}
