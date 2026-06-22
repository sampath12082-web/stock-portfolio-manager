package com.stocks.myportfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocks.myportfolio.common.enums.SignalStatus;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.TradingSignal;
import com.stocks.myportfolio.integration.StockQuoteData;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.repository.TradingSignalRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;

@Service
public class AiStockService {

    private static final Logger log = LoggerFactory.getLogger(AiStockService.class);

    private final MarketDataService marketDataService;
    private final StockLookupService stockLookupService;
    private final TradingSignalRepository signalRepository;
    private final HoldingRepository holdingRepository;
    private final CurrentUserProvider currentUser;
    private final ClaudeApiClient claudeApi;

    public AiStockService(MarketDataService marketDataService, StockLookupService stockLookupService,
            TradingSignalRepository signalRepository, HoldingRepository holdingRepository,
            CurrentUserProvider currentUser, ClaudeApiClient claudeApi) {
        this.marketDataService = marketDataService;
        this.stockLookupService = stockLookupService;
        this.signalRepository = signalRepository;
        this.holdingRepository = holdingRepository;
        this.currentUser = currentUser;
        this.claudeApi = claudeApi;
    }

    public Map<String, Object> chat(String prompt) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("prompt", prompt);

        String context = buildContext(prompt);

        if (claudeApi.isAvailable()) {
            try {
                String response = callClaude(prompt, context);
                result.put("response", response);
                result.put("source", "claude");
            } catch (Exception e) {
                log.warn("Claude API failed: {}", e.getMessage());
                result.put("response", generateLocalResponse(prompt, context));
                result.put("source", "local");
            }
        } else {
            result.put("response", generateLocalResponse(prompt, context));
            result.put("source", "local");
        }

        String stockSymbol = extractStockSymbol(prompt);
        if (stockSymbol != null) {
            result.put("stockData", getStockData(stockSymbol));
        }

        return result;
    }

    private String buildContext(String prompt) {
        StringBuilder ctx = new StringBuilder();

        List<Holding> activeHoldings = holdingRepository.findByUserId(currentUser.getUserId()).stream()
                .filter(h -> h.getQuantity() > 0).toList();

        ctx.append("USER PORTFOLIO: ");
        if (activeHoldings.isEmpty()) {
            ctx.append("No active holdings.\n");
        } else {
            BigDecimal totalInvested = BigDecimal.ZERO;
            BigDecimal totalCurrent = BigDecimal.ZERO;
            Map<String, StockQuoteData> prices = Map.of();
            try { prices = marketDataService.getCurrentPrices(); } catch (Exception e) { /* */ }

            for (Holding h : activeHoldings) {
                StockQuoteData q = prices.get(h.getStock().getSymbol());
                BigDecimal current = q != null && q.ltp() != null
                        ? q.ltp().multiply(BigDecimal.valueOf(h.getQuantity()))
                        : h.getInvestedAmount();
                totalInvested = totalInvested.add(h.getInvestedAmount());
                totalCurrent = totalCurrent.add(current);
                ctx.append(String.format("%s: %d shares @ ₹%s (current ₹%s), ",
                        h.getStock().getSymbol(), h.getQuantity(),
                        h.getAverageBuyPrice().setScale(2, RoundingMode.HALF_UP),
                        q != null && q.ltp() != null ? q.ltp().setScale(2, RoundingMode.HALF_UP) : "N/A"));
            }
            BigDecimal pnl = totalCurrent.subtract(totalInvested);
            ctx.append(String.format("\nTotal: Invested ₹%s, Current ₹%s, P&L ₹%s.\n",
                    totalInvested.setScale(0, RoundingMode.HALF_UP),
                    totalCurrent.setScale(0, RoundingMode.HALF_UP),
                    pnl.setScale(0, RoundingMode.HALF_UP)));
        }

        List<TradingSignal> signals = signalRepository.findByStatusOrderBySignalDateDesc(SignalStatus.ACTIVE);
        if (!signals.isEmpty()) {
            ctx.append("ACTIVE SIGNALS: ");
            signals.stream().limit(10).forEach(s ->
                    ctx.append(String.format("%s=%s(target ₹%s), ", s.getSymbol(), s.getSignalType(),
                            s.getTargetPrice() != null ? s.getTargetPrice() : "N/A")));
            ctx.append("\n");
        }

        String symbol = extractStockSymbol(prompt);
        if (symbol != null) {
            Map<String, Object> stockData = getStockData(symbol);
            if (stockData.containsKey("ltp")) {
                ctx.append(String.format("QUERIED STOCK %s: LTP ₹%s, Day %s%%, Signal: %s",
                        symbol, stockData.get("ltp"), stockData.get("dayChangePct"),
                        stockData.get("signalType")));
                if (stockData.get("targetPrice") != null) {
                    ctx.append(String.format(", Target ₹%s", stockData.get("targetPrice")));
                }
                ctx.append("\n");
            }
        }

        return ctx.toString();
    }

    private String extractStockSymbol(String prompt) {
        String upper = prompt.toUpperCase().trim();
        String[] words = upper.split("\\s+");
        for (String word : words) {
            String clean = word.replaceAll("[^A-Z0-9&-]", "");
            if (clean.length() >= 2 && clean.length() <= 15 && clean.matches("[A-Z][A-Z0-9&-]*")) {
                var results = stockLookupService.lookup(clean);
                if (!results.isEmpty() && results.get(0).symbol().equalsIgnoreCase(clean)) {
                    return results.get(0).symbol();
                }
            }
        }
        return null;
    }

    public Map<String, Object> getStockData(String symbol) {
        Map<String, Object> data = new LinkedHashMap<>();
        var results = stockLookupService.lookup(symbol);
        if (results.isEmpty()) return data;

        var match = results.get(0);
        data.put("symbol", match.symbol());
        data.put("companyName", match.companyName());
        data.put("exchange", match.exchange());
        data.put("sector", match.sector());
        data.put("industry", match.industry());
        data.put("existsInDb", match.existsInDb());

        try {
            Map<String, StockQuoteData> prices = marketDataService.getCurrentPrices();
            StockQuoteData q = prices.get(match.symbol());
            if (q != null) {
                data.put("ltp", q.ltp());
                data.put("open", q.open());
                data.put("high", q.high());
                data.put("low", q.low());
                data.put("previousClose", q.previousClose());
                data.put("volume", q.volume());
                if (q.ltp() != null && q.previousClose() != null && q.previousClose().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal change = q.ltp().subtract(q.previousClose());
                    data.put("dayChange", change);
                    data.put("dayChangePct", change.multiply(BigDecimal.valueOf(100)).divide(q.previousClose(), 2, RoundingMode.HALF_UP));
                }
            }
        } catch (Exception e) { /* */ }

        signalRepository.findBySymbolIgnoreCaseOrderBySignalDateDesc(symbol).stream()
                .filter(s -> s.getStatus() == SignalStatus.ACTIVE)
                .findFirst()
                .ifPresentOrElse(sig -> {
                    data.put("signalType", sig.getSignalType().name());
                    data.put("targetPrice", sig.getTargetPrice());
                    data.put("signalDate", sig.getSignalDate());
                    data.put("rationale", sig.getRationale());
                }, () -> data.put("signalType", "NO_SIGNAL"));

        return data;
    }

    private String callClaude(String prompt, String context) throws Exception {
        String systemPrompt = "You are a stock market assistant for an Indian stock portfolio app (NSE/BSE). " +
                "You have access to the user's portfolio data, live prices, and technical analysis signals. " +
                "Answer questions about stocks, market trends, portfolio analysis, trading strategies, sector analysis, etc. " +
                "When mentioning stocks, include their current price and signal if available. " +
                "Keep responses concise (3-5 sentences). Always include a clear recommendation when relevant. " +
                "Disclaimer: This is for educational purposes only, not financial advice.";
        return claudeApi.call(systemPrompt, String.format("Context:\n%s\n\nUser question: %s", context, prompt));
    }

    private String generateLocalResponse(String prompt, String context) {
        String lower = prompt.toLowerCase();

        if (lower.contains("portfolio") || lower.contains("my stocks") || lower.contains("my holdings")) {
            return generatePortfolioResponse(context);
        }
        if (lower.contains("signal") || lower.contains("buy") || lower.contains("sell") || lower.contains("recommend")) {
            return generateSignalResponse();
        }
        if (lower.contains("sector") || lower.contains("industry")) {
            return "Sector analysis requires Claude AI. Set ANTHROPIC_API_KEY env var for AI-powered responses. " +
                    "Your portfolio is concentrated in Materials (HINDALCO, JSWSTEEL). Consider diversifying.";
        }

        String symbol = extractStockSymbol(prompt);
        if (symbol != null) {
            return generateStockResponse(symbol);
        }

        return "I can help with: stock analysis (type a symbol like TCS or RELIANCE), portfolio review (ask 'how is my portfolio?'), " +
                "trading signals (ask 'what should I buy/sell?'), and market insights. " +
                "For richer AI analysis, set ANTHROPIC_API_KEY environment variable.";
    }

    private String generatePortfolioResponse(String context) {
        List<Holding> active = holdingRepository.findByUserId(currentUser.getUserId()).stream().filter(h -> h.getQuantity() > 0).toList();
        if (active.isEmpty()) return "You have no active stock holdings.";

        BigDecimal totalInv = active.stream().map(Holding::getInvestedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return String.format("Your portfolio has %d stocks with ₹%s invested. %s",
                active.size(), totalInv.setScale(0, RoundingMode.HALF_UP),
                "Check the Holdings page for detailed P&L breakdown and the Dashboard for overall performance.");
    }

    private String generateSignalResponse() {
        List<TradingSignal> signals = signalRepository.findByStatusOrderBySignalDateDesc(SignalStatus.ACTIVE);
        if (signals.isEmpty()) return "No active trading signals. Run analysis from the Dashboard to generate new signals.";

        long buys = signals.stream().filter(s -> s.getSignalType().name().contains("BUY")).count();
        long sells = signals.stream().filter(s -> s.getSignalType().name().contains("SELL")).count();

        String topBuy = signals.stream().filter(s -> s.getSignalType().name().contains("BUY")).findFirst()
                .map(s -> s.getSymbol() + " (target ₹" + s.getTargetPrice() + ")").orElse("none");
        String topSell = signals.stream().filter(s -> s.getSignalType().name().contains("SELL")).findFirst()
                .map(s -> s.getSymbol() + " (target ₹" + s.getTargetPrice() + ")").orElse("none");

        return String.format("%d active signals: %d BUY, %d SELL. Top BUY: %s. Top SELL: %s. " +
                "Signals are based on SMA crossover, RSI, and 52-week analysis.", signals.size(), buys, sells, topBuy, topSell);
    }

    private String generateStockResponse(String symbol) {
        Map<String, Object> data = getStockData(symbol);
        if (data.isEmpty()) return "Could not find data for " + symbol;

        String signal = String.valueOf(data.getOrDefault("signalType", "NO_SIGNAL"));
        Object ltp = data.get("ltp");
        Object changePct = data.get("dayChangePct");
        Object target = data.get("targetPrice");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s (%s)", symbol, data.get("companyName")));
        if (ltp != null) sb.append(String.format(" trading at ₹%s", ltp));
        if (changePct != null) sb.append(String.format(" (%s%% today)", changePct));
        sb.append(". ");

        switch (signal) {
            case "BUY_SIGNAL" -> {
                sb.append("Signal: BUY. ");
                if (target != null) sb.append(String.format("Target ₹%s. ", target));
                sb.append("Technical indicators suggest accumulation.");
            }
            case "SELL_SIGNAL" -> {
                sb.append("Signal: SELL. ");
                if (target != null) sb.append(String.format("Target ₹%s. ", target));
                sb.append("Consider booking profits.");
            }
            case "HOLD" -> sb.append("Signal: HOLD. No strong directional bias currently.");
            default -> sb.append("No active signal. Run analysis to generate.");
        }
        return sb.toString();
    }
}
