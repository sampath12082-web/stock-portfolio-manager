package com.stocks.myportfolio.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocks.myportfolio.common.enums.Exchange;
import com.stocks.myportfolio.common.enums.SignalSource;
import com.stocks.myportfolio.common.enums.SignalStatus;
import com.stocks.myportfolio.common.enums.SignalType;
import com.stocks.myportfolio.dto.response.TradingSignalResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.entity.TradingSignal;
import com.stocks.myportfolio.integration.yahoo.HistoricalQuote;
import com.stocks.myportfolio.integration.yahoo.YahooFinanceProvider;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import com.stocks.myportfolio.repository.TradingSignalRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import com.stocks.myportfolio.service.TechnicalAnalysisService;

@Service
public class TechnicalAnalysisServiceImpl implements TechnicalAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(TechnicalAnalysisServiceImpl.class);

    private static final List<String> WATCHLIST = List.of(
            "RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK",
            "SBIN", "BHARTIARTL", "ITC", "LT", "KOTAKBANK",
            "AXISBANK", "SUNPHARMA", "MARUTI", "TITAN", "BAJFINANCE",
            "HCLTECH", "ASIANPAINT", "WIPRO", "TATASTEEL", "POWERGRID",
            "NTPC", "ULTRACEMCO", "NESTLEIND", "BAJAJFINSV", "TECHM",
            "HINDALCO", "JSWSTEEL", "ADANIPORTS", "COALINDIA", "DRREDDY");

    private final YahooFinanceProvider yahooProvider;
    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;
    private final TradingSignalRepository signalRepository;
    private final CurrentUserProvider currentUser;

    public TechnicalAnalysisServiceImpl(
            YahooFinanceProvider yahooProvider,
            HoldingRepository holdingRepository,
            StockRepository stockRepository,
            TradingSignalRepository signalRepository,
            CurrentUserProvider currentUser) {

        this.yahooProvider = yahooProvider;
        this.holdingRepository = holdingRepository;
        this.stockRepository = stockRepository;
        this.signalRepository = signalRepository;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional
    public List<TradingSignalResponse> analyzeHoldings() {
        List<Holding> holdings = holdingRepository.findByUserId(currentUser.getUserId());
        List<TradingSignalResponse> results = new ArrayList<>();

        for (Holding holding : holdings) {
            try {
                Stock stock = holding.getStock();
                List<HistoricalQuote> history = yahooProvider.fetchHistoricalData(
                        stock.getSymbol(), stock.getExchange());

                if (history.size() < 20) continue;

                AnalysisResult analysis = analyze(history);
                TradingSignal signal = createSignal(stock, analysis);
                signalRepository.save(signal);
                results.add(toResponse(signal));
            } catch (Exception e) {
                log.warn("Analysis failed for {}: {}", holding.getStock().getSymbol(), e.getMessage());
            }
        }

        return results;
    }

    @Override
    @Transactional
    public List<TradingSignalResponse> scanMarket() {
        List<TradingSignalResponse> results = new ArrayList<>();

        // PRIORITY 1: Analyze portfolio stocks (BUY/SELL/HOLD)
        List<Holding> holdings = holdingRepository.findByUserId(currentUser.getUserId());
        java.util.Set<String> portfolioSymbols = new java.util.HashSet<>();
        for (Holding holding : holdings) {
            if (holding.getQuantity() <= 0) continue;
            try {
                Stock stock = holding.getStock();
                portfolioSymbols.add(stock.getSymbol());
                List<HistoricalQuote> history = yahooProvider.fetchHistoricalData(
                        stock.getSymbol(), stock.getExchange());
                if (history.size() < 20) continue;

                AnalysisResult analysis = analyze(history);
                TradingSignal signal = createSignal(stock, analysis);
                signal.setNotes("Portfolio stock. " + (signal.getNotes() != null ? signal.getNotes() : ""));
                signalRepository.save(signal);
                results.add(toResponse(signal));
            } catch (Exception e) {
                log.warn("Portfolio analysis failed for {}: {}", holding.getStock().getSymbol(), e.getMessage());
            }
        }

        // PRIORITY 2: Scan watchlist for additional BUY picks (exclude portfolio stocks)
        List<AnalysisCandidate> candidates = new ArrayList<>();
        for (String symbol : WATCHLIST) {
            if (portfolioSymbols.contains(symbol)) continue;
            try {
                List<HistoricalQuote> history = yahooProvider.fetchHistoricalData(
                        symbol, Exchange.NSE);
                if (history.size() < 20) continue;

                AnalysisResult analysis = analyze(history);
                if (analysis.signalType == SignalType.BUY_SIGNAL) {
                    candidates.add(new AnalysisCandidate(symbol, analysis));
                }
            } catch (Exception e) {
                log.warn("Market scan failed for {}: {}", symbol, e.getMessage());
            }
        }

        candidates.sort(Comparator.comparingInt(
                (AnalysisCandidate c) -> c.analysis.score).reversed());

        for (AnalysisCandidate c : candidates.stream().limit(5).toList()) {
            Stock stock = stockRepository.findBySymbolIgnoreCase(c.symbol).orElse(null);
            TradingSignal signal = new TradingSignal();
            signal.setStock(stock);
            signal.setSymbol(c.symbol);
            signal.setSignalType(SignalType.BUY_SIGNAL);
            signal.setTargetPrice(c.analysis.targetPrice);
            signal.setStopLoss(c.analysis.stopLoss);
            signal.setCurrentPrice(c.analysis.currentPrice);
            signal.setRationale(c.analysis.rationale);
            signal.setSignalDate(LocalDate.now());
            signal.setStatus(SignalStatus.ACTIVE);
            signal.setSource(SignalSource.AUTO);
            signal.setNotes("Timeframe: " + c.analysis.timeframe);
            signalRepository.save(signal);
            results.add(toResponse(signal));
        }

        return results;
    }

    @Override
    @Transactional
    public void runDailyAnalysis() {
        log.info("Running daily technical analysis");
        analyzeHoldings();
        scanMarket();
        log.info("Daily analysis complete");
    }

    private AnalysisResult analyze(List<HistoricalQuote> history) {
        BigDecimal currentPrice = history.get(history.size() - 1).close();
        BigDecimal sma20 = sma(history, 20);
        BigDecimal sma50 = history.size() >= 50 ? sma(history, 50) : sma20;
        double rsi = rsi(history, 14);

        BigDecimal high52w = history.stream()
                .map(HistoricalQuote::high)
                .filter(h -> h != null)
                .max(Comparator.naturalOrder())
                .orElse(currentPrice);
        BigDecimal low52w = history.stream()
                .map(HistoricalQuote::low)
                .filter(l -> l != null)
                .min(Comparator.naturalOrder())
                .orElse(currentPrice);

        double avgVol5 = history.subList(Math.max(0, history.size() - 5), history.size())
                .stream().mapToLong(HistoricalQuote::volume).average().orElse(0);
        double avgVol20 = history.subList(Math.max(0, history.size() - 20), history.size())
                .stream().mapToLong(HistoricalQuote::volume).average().orElse(1);
        boolean highVolume = avgVol5 > avgVol20 * 1.2;

        int buyScore = 0;
        int sellScore = 0;
        List<String> reasons = new ArrayList<>();

        if (sma20.compareTo(sma50) > 0) {
            buyScore += 2;
            reasons.add("Golden cross (SMA20 > SMA50)");
        } else {
            sellScore += 2;
            reasons.add("Death cross (SMA20 < SMA50)");
        }

        if (rsi < 30) {
            buyScore += 3;
            reasons.add(String.format("RSI oversold (%.1f)", rsi));
        } else if (rsi > 70) {
            sellScore += 3;
            reasons.add(String.format("RSI overbought (%.1f)", rsi));
        } else {
            reasons.add(String.format("RSI neutral (%.1f)", rsi));
        }

        BigDecimal range52w = high52w.subtract(low52w);
        if (range52w.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal positionInRange = currentPrice.subtract(low52w)
                    .divide(range52w, 4, RoundingMode.HALF_UP);
            if (positionInRange.doubleValue() < 0.2) {
                buyScore += 2;
                reasons.add("Near 52-week low");
            } else if (positionInRange.doubleValue() > 0.9) {
                sellScore += 1;
                reasons.add("Near 52-week high");
            }
        }

        if (highVolume) {
            reasons.add("High volume confirms trend");
            if (buyScore > sellScore) buyScore += 1;
            else sellScore += 1;
        }

        SignalType signalType;
        if (buyScore >= sellScore + 2) {
            signalType = SignalType.BUY_SIGNAL;
        } else if (sellScore >= buyScore + 2) {
            signalType = SignalType.SELL_SIGNAL;
        } else {
            signalType = SignalType.HOLD;
        }

        BigDecimal targetPrice = signalType == SignalType.BUY_SIGNAL
                ? currentPrice.multiply(BigDecimal.valueOf(1.10)).setScale(2, RoundingMode.HALF_UP)
                : high52w;
        BigDecimal stopLoss = signalType == SignalType.SELL_SIGNAL
                ? null
                : currentPrice.multiply(BigDecimal.valueOf(0.95)).setScale(2, RoundingMode.HALF_UP);

        String timeframe = signalType == SignalType.BUY_SIGNAL ? "2-4 weeks" : "1-2 weeks";

        return new AnalysisResult(
                signalType,
                buyScore + sellScore,
                currentPrice,
                targetPrice,
                stopLoss,
                String.join(". ", reasons),
                timeframe);
    }

    private BigDecimal sma(List<HistoricalQuote> history, int period) {
        int start = Math.max(0, history.size() - period);
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (int i = start; i < history.size(); i++) {
            if (history.get(i).close() != null) {
                sum = sum.add(history.get(i).close());
                count++;
            }
        }
        return count > 0
                ? sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    private double rsi(List<HistoricalQuote> history, int period) {
        if (history.size() < period + 1) return 50;

        double avgGain = 0;
        double avgLoss = 0;
        int start = history.size() - period - 1;

        for (int i = start + 1; i < history.size(); i++) {
            BigDecimal prev = history.get(i - 1).close();
            BigDecimal curr = history.get(i).close();
            if (prev == null || curr == null) continue;

            double change = curr.subtract(prev).doubleValue();
            if (change > 0) avgGain += change;
            else avgLoss -= change;
        }

        avgGain /= period;
        avgLoss /= period;

        if (avgLoss == 0) return 100;
        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    private TradingSignal createSignal(Stock stock, AnalysisResult analysis) {
        TradingSignal signal = new TradingSignal();
        signal.setStock(stock);
        signal.setSymbol(stock.getSymbol());
        signal.setSignalType(analysis.signalType);
        signal.setTargetPrice(analysis.targetPrice);
        signal.setStopLoss(analysis.stopLoss);
        signal.setCurrentPrice(analysis.currentPrice);
        signal.setRationale(analysis.rationale);
        signal.setSignalDate(LocalDate.now());
        signal.setStatus(SignalStatus.ACTIVE);
        signal.setSource(SignalSource.AUTO);
        signal.setNotes("Timeframe: " + analysis.timeframe);
        return signal;
    }

    private TradingSignalResponse toResponse(TradingSignal signal) {
        return new TradingSignalResponse(
                signal.getId(),
                signal.getSymbol(),
                signal.getStock() != null ? signal.getStock().getCompanyName() : null,
                signal.getSignalType(),
                signal.getTargetPrice(),
                signal.getStopLoss(),
                signal.getCurrentPrice(),
                signal.getRationale(),
                signal.getSignalDate(),
                signal.getStatus(),
                signal.getNotes(),
                signal.getCreatedAt());
    }

    private record AnalysisResult(
            SignalType signalType,
            int score,
            BigDecimal currentPrice,
            BigDecimal targetPrice,
            BigDecimal stopLoss,
            String rationale,
            String timeframe) {
    }

    private record AnalysisCandidate(String symbol, AnalysisResult analysis) {
    }
}
