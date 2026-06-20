package com.stocks.myportfolio.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stocks.myportfolio.common.exception.MarketDataException;
import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.common.util.CalculationUtils;
import com.stocks.myportfolio.dto.response.PortfolioSnapshotResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.PortfolioSnapshot;
import com.stocks.myportfolio.integration.StockQuoteData;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.repository.PortfolioSnapshotRepository;
import com.stocks.myportfolio.service.MarketDataService;
import com.stocks.myportfolio.service.PerformanceService;

@Service
public class PerformanceServiceImpl implements PerformanceService {

    private final PortfolioSnapshotRepository snapshotRepository;
    private final HoldingRepository holdingRepository;
    private final MarketDataService marketDataService;

    public PerformanceServiceImpl(
            PortfolioSnapshotRepository snapshotRepository,
            HoldingRepository holdingRepository,
            MarketDataService marketDataService) {

        this.snapshotRepository = snapshotRepository;
        this.holdingRepository = holdingRepository;
        this.marketDataService = marketDataService;
    }

    @Override
    public List<PortfolioSnapshotResponse> getPerformanceHistory(
            LocalDate from, LocalDate to) {

        return snapshotRepository
                .findBySnapshotDateBetweenOrderBySnapshotDateAsc(from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<PortfolioSnapshotResponse> getRecentPerformance(int days) {
        LocalDate from = LocalDate.now().minusDays(days);
        LocalDate to = LocalDate.now();
        return getPerformanceHistory(from, to);
    }

    @Override
    public PortfolioSnapshotResponse getTodaySnapshot() {
        return snapshotRepository.findTopByOrderBySnapshotDateDesc()
                .map(this::toResponse)
                .orElseGet(() -> captureSnapshot());
    }

    @Override
    public PortfolioSnapshotResponse captureSnapshot() {
        LocalDate today = LocalDate.now();

        return snapshotRepository.findBySnapshotDate(today)
                .map(this::toResponse)
                .orElseGet(() -> toResponse(createSnapshot(today)));
    }

    private PortfolioSnapshot createSnapshot(LocalDate date) {
        List<Holding> holdings = holdingRepository.findAll().stream()
                .filter(h -> h.getQuantity() > 0)
                .toList();

        Map<String, StockQuoteData> prices;
        try {
            marketDataService.refreshAllQuotes();
            prices = marketDataService.getCurrentPrices();
        } catch (MarketDataException e) {
            prices = Map.of();
        }

        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        String topGainer = null;
        String topLoser = null;
        BigDecimal bestPnlPct = null;
        BigDecimal worstPnlPct = null;

        for (Holding holding : holdings) {
            totalInvestment = totalInvestment.add(holding.getInvestedAmount());
            StockQuoteData quote = prices.get(holding.getStock().getSymbol());

            BigDecimal holdingValue;
            if (quote != null && quote.ltp() != null) {
                holdingValue = quote.ltp().multiply(
                        BigDecimal.valueOf(holding.getQuantity()));
                BigDecimal pnl = CalculationUtils.calculatePnL(
                        holdingValue, holding.getInvestedAmount());
                BigDecimal pnlPct = CalculationUtils.calculatePnLPercentage(
                        pnl, holding.getInvestedAmount());

                if (bestPnlPct == null || pnlPct.compareTo(bestPnlPct) > 0) {
                    bestPnlPct = pnlPct;
                    topGainer = holding.getStock().getSymbol();
                }
                if (worstPnlPct == null || pnlPct.compareTo(worstPnlPct) < 0) {
                    worstPnlPct = pnlPct;
                    topLoser = holding.getStock().getSymbol();
                }
            } else {
                holdingValue = holding.getInvestedAmount();
            }
            currentValue = currentValue.add(holdingValue);
        }

        BigDecimal totalPnL = CalculationUtils.calculatePnL(currentValue, totalInvestment);
        BigDecimal totalPnLPercentage = CalculationUtils.calculatePnLPercentage(
                totalPnL, totalInvestment);

        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setSnapshotDate(date);
        snapshot.setTotalInvestment(totalInvestment);
        snapshot.setCurrentValue(currentValue);
        snapshot.setTotalPnL(totalPnL);
        snapshot.setTotalPnLPercentage(totalPnLPercentage);
        snapshot.setHoldingCount(holdings.size());
        snapshot.setTopGainer(topGainer);
        snapshot.setTopLoser(topLoser);

        return snapshotRepository.save(snapshot);
    }

    private PortfolioSnapshotResponse toResponse(PortfolioSnapshot snapshot) {
        return new PortfolioSnapshotResponse(
                snapshot.getId(),
                snapshot.getSnapshotDate(),
                snapshot.getTotalInvestment(),
                snapshot.getCurrentValue(),
                snapshot.getTotalPnL(),
                snapshot.getTotalPnLPercentage(),
                snapshot.getHoldingCount(),
                snapshot.getTopGainer(),
                snapshot.getTopLoser());
    }
}
