package com.stocks.myportfolio.service;

import java.math.BigDecimal;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocks.myportfolio.entity.TradingSignal;
import com.stocks.myportfolio.integration.StockQuoteData;
import com.stocks.myportfolio.repository.TradingSignalRepository;
import com.stocks.myportfolio.service.MarketDataService;
import com.stocks.myportfolio.service.StockLookupService;

@Service
public class AiStockService {

    private static final Logger log = LoggerFactory.getLogger(AiStockService.class);

    @Value("${anthropic.api-key:}")
    private String anthropicApiKey;

    private final MarketDataService marketDataService;
    private final StockLookupService stockLookupService;
    private final TradingSignalRepository signalRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiStockService(MarketDataService marketDataService, StockLookupService stockLookupService,
            TradingSignalRepository signalRepository) {
        this.marketDataService = marketDataService;
        this.stockLookupService = stockLookupService;
        this.signalRepository = signalRepository;
    }

    public Map<String, Object> searchAndAnalyze(String query) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", query);

        // Step 1: Search for the stock
        var lookupResults = stockLookupService.lookup(query);
        if (lookupResults.isEmpty()) {
            result.put("error", "No stocks found for: " + query);
            return result;
        }

        var match = lookupResults.get(0);
        String symbol = match.symbol();
        result.put("symbol", symbol);
        result.put("companyName", match.companyName());
        result.put("exchange", match.exchange());
        result.put("sector", match.sector());
        result.put("industry", match.industry());
        result.put("existsInDb", match.existsInDb());

        // Step 2: Get live quote
        try {
            Map<String, StockQuoteData> prices = marketDataService.getCurrentPrices();
            StockQuoteData quote = prices.get(symbol);
            if (quote != null) {
                result.put("ltp", quote.ltp());
                result.put("open", quote.open());
                result.put("high", quote.high());
                result.put("low", quote.low());
                result.put("previousClose", quote.previousClose());
                result.put("volume", quote.volume());
                BigDecimal change = quote.ltp() != null && quote.previousClose() != null
                        ? quote.ltp().subtract(quote.previousClose()) : null;
                BigDecimal changePct = change != null && quote.previousClose() != null
                        && quote.previousClose().compareTo(BigDecimal.ZERO) > 0
                        ? change.multiply(BigDecimal.valueOf(100)).divide(quote.previousClose(), 2, java.math.RoundingMode.HALF_UP)
                        : null;
                result.put("dayChange", change);
                result.put("dayChangePct", changePct);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch quote for {}: {}", symbol, e.getMessage());
        }

        // Step 3: Get trading signal
        List<TradingSignal> signals = signalRepository.findBySymbolIgnoreCaseOrderBySignalDateDesc(symbol).stream()
                .filter(s -> s.getStatus() == com.stocks.myportfolio.common.enums.SignalStatus.ACTIVE)
                .toList();
        if (!signals.isEmpty()) {
            TradingSignal sig = signals.get(0);
            result.put("signalType", sig.getSignalType().name());
            result.put("targetPrice", sig.getTargetPrice());
            result.put("signalDate", sig.getSignalDate());
            result.put("rationale", sig.getRationale());
        } else {
            result.put("signalType", "NO_SIGNAL");
        }

        // Step 4: AI analysis (if API key configured)
        if (anthropicApiKey != null && !anthropicApiKey.isBlank()) {
            try {
                String aiAnalysis = getAiAnalysis(symbol, result);
                result.put("aiAnalysis", aiAnalysis);
            } catch (Exception e) {
                log.warn("AI analysis failed: {}", e.getMessage());
                result.put("aiAnalysis", "AI analysis unavailable");
            }
        } else {
            result.put("aiAnalysis", generateLocalAnalysis(result));
        }

        return result;
    }

    private String getAiAnalysis(String symbol, Map<String, Object> data) throws Exception {
        String prompt = String.format(
                "Analyze Indian stock %s (%s). Current price: %s, Day change: %s%%, Signal: %s, Target: %s, Sector: %s. " +
                "Provide a brief 3-4 sentence analysis covering: 1) Current trend 2) Key risk 3) Trade recommendation (buy/sell/hold). " +
                "Be concise and specific. This is for educational purposes only.",
                symbol, data.get("companyName"), data.get("ltp"), data.get("dayChangePct"),
                data.get("signalType"), data.get("targetPrice"), data.get("sector"));

        RestClient client = RestClient.create();
        String response = client.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", anthropicApiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .body(String.format("""
                    {"model":"claude-sonnet-4-6","max_tokens":300,"messages":[{"role":"user","content":"%s"}]}
                    """, prompt.replace("\"", "\\\"").replace("\n", "\\n")))
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);
        return root.path("content").get(0).path("text").asText();
    }

    private String generateLocalAnalysis(Map<String, Object> data) {
        String signal = String.valueOf(data.getOrDefault("signalType", "NO_SIGNAL"));
        Object ltpObj = data.get("ltp");
        Object targetObj = data.get("targetPrice");
        Object changePctObj = data.get("dayChangePct");
        String sector = String.valueOf(data.getOrDefault("sector", "Unknown"));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s (%s) in %s sector. ", data.get("symbol"), data.get("companyName"), sector));

        if (ltpObj != null) {
            sb.append(String.format("Trading at ₹%s", ltpObj));
            if (changePctObj != null) {
                BigDecimal pct = new BigDecimal(changePctObj.toString());
                sb.append(String.format(" (%s%s%% today). ", pct.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "", pct));
            } else {
                sb.append(". ");
            }
        }

        switch (signal) {
            case "BUY_SIGNAL" -> {
                sb.append("Technical signals indicate BUY. ");
                if (targetObj != null) sb.append(String.format("Target price: ₹%s. ", targetObj));
                sb.append("Consider accumulating on dips.");
            }
            case "SELL_SIGNAL" -> {
                sb.append("Technical signals indicate SELL. ");
                if (targetObj != null) sb.append(String.format("Target price: ₹%s. ", targetObj));
                sb.append("Consider booking profits or setting stop-loss.");
            }
            case "HOLD" -> sb.append("Technical signals suggest HOLD. No strong directional bias. Wait for clearer trend.");
            default -> sb.append("No active trading signal. Run technical analysis to generate signals.");
        }

        return sb.toString();
    }
}
