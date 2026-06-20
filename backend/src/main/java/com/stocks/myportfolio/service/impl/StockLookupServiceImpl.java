package com.stocks.myportfolio.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import com.stocks.myportfolio.dto.response.StockLookupResponse;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.service.StockLookupService;

@Service
public class StockLookupServiceImpl implements StockLookupService {

    private static final Logger log = LoggerFactory.getLogger(StockLookupServiceImpl.class);

    private final StockRepository stockRepository;
    private final RestClient yahooClient;

    public StockLookupServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
        this.yahooClient = RestClient.builder()
                .baseUrl("https://query1.finance.yahoo.com")
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    public List<StockLookupResponse> lookup(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<Stock> dbResults = stockRepository
                .findBySymbolContainingIgnoreCase(query.trim());

        Set<String> dbSymbols = new HashSet<>();
        List<StockLookupResponse> results = new ArrayList<>();

        for (Stock stock : dbResults) {
            dbSymbols.add(stock.getSymbol().toUpperCase());
            results.add(new StockLookupResponse(
                    stock.getSymbol(),
                    stock.getCompanyName(),
                    stock.getExchange() != null ? stock.getExchange().name() : null,
                    stock.getSector(),
                    stock.getIndustry(),
                    true));
        }

        try {
            List<StockLookupResponse> webResults = searchYahooFinance(query.trim());
            for (StockLookupResponse webResult : webResults) {
                if (!dbSymbols.contains(webResult.symbol().toUpperCase())) {
                    results.add(webResult);
                }
            }
        } catch (Exception e) {
            log.warn("Yahoo Finance search failed for '{}': {}", query, e.getMessage());
        }

        return results;
    }

    private List<StockLookupResponse> searchYahooFinance(String query) {
        JsonNode root = yahooClient.get()
                .uri("/v1/finance/search?q={q}&quotesCount=10&newsCount=0", query)
                .retrieve()
                .body(JsonNode.class);

        if (root == null) {
            return List.of();
        }

        JsonNode quotes = root.path("quotes");
        List<StockLookupResponse> results = new ArrayList<>();

        for (JsonNode quote : quotes) {
            String quoteType = quote.path("quoteType").asText("");
            if (!"EQUITY".equals(quoteType)) {
                continue;
            }

            String exchDisp = quote.path("exchDisp").asText("");
            if (!"NSE".equals(exchDisp) && !"Bombay".equals(exchDisp) && !"BSE".equals(exchDisp)) {
                continue;
            }

            String yahooSymbol = quote.path("symbol").asText("");
            String nseSymbol = yahooSymbol
                    .replace(".NS", "")
                    .replace(".BO", "");

            String exchange = "NSE";
            if (yahooSymbol.endsWith(".BO") || "Bombay".equals(exchDisp) || "BSE".equals(exchDisp)) {
                exchange = "BSE";
            }

            String longName = quote.path("longname").asText(
                    quote.path("shortname").asText(nseSymbol));
            String sector = quote.path("sectorDisp").asText(null);
            String industry = quote.path("industryDisp").asText(null);

            results.add(new StockLookupResponse(
                    nseSymbol,
                    longName,
                    exchange,
                    sector,
                    industry,
                    false));
        }

        return results;
    }
}
