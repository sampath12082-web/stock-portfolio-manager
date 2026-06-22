package com.stocks.myportfolio.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stocks.myportfolio.common.exception.MarketDataException;
import com.stocks.myportfolio.common.util.CalculationUtils;
import com.stocks.myportfolio.dto.response.PortfolioSummaryResponse;
import com.stocks.myportfolio.dto.response.SectorAllocationResponse;
import com.stocks.myportfolio.dto.response.StockPnLResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.integration.StockQuoteData;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import com.stocks.myportfolio.service.MarketDataService;
import com.stocks.myportfolio.service.PortfolioService;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final HoldingRepository holdingRepository;
    private final MarketDataService marketDataService;
    private final CurrentUserProvider currentUser;

    public PortfolioServiceImpl(
            HoldingRepository holdingRepository,
            MarketDataService marketDataService, CurrentUserProvider currentUser) {

        this.holdingRepository = holdingRepository;
        this.marketDataService = marketDataService;
        this.currentUser = currentUser;
    }

    @Override
    public PortfolioSummaryResponse getPortfolioSummary() {
        List<Holding> holdings = holdingRepository.findByUserId(currentUser.getUserId());
        Map<String, StockQuoteData> prices = fetchPrices();

        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        BigDecimal previousValue = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            totalInvestment = totalInvestment.add(holding.getInvestedAmount());
            StockQuoteData quote = prices.get(holding.getStock().getSymbol());

            if (quote != null && quote.ltp() != null) {
                BigDecimal qty = BigDecimal.valueOf(holding.getQuantity());
                currentValue = currentValue.add(quote.ltp().multiply(qty));
                if (quote.previousClose() != null) {
                    previousValue = previousValue.add(quote.previousClose().multiply(qty));
                } else {
                    previousValue = previousValue.add(quote.ltp().multiply(qty));
                }
            } else {
                currentValue = currentValue.add(holding.getInvestedAmount());
                previousValue = previousValue.add(holding.getInvestedAmount());
            }
        }

        BigDecimal totalPnL = CalculationUtils.calculatePnL(currentValue, totalInvestment);
        BigDecimal totalPnLPercentage = CalculationUtils.calculatePnLPercentage(
                totalPnL, totalInvestment);
        BigDecimal dayPnL = CalculationUtils.calculatePnL(currentValue, previousValue);
        BigDecimal dayPnLPercentage = CalculationUtils.calculatePnLPercentage(
                dayPnL, previousValue);

        return new PortfolioSummaryResponse(
                holdings.size(),
                totalInvestment,
                currentValue,
                totalPnL,
                totalPnLPercentage,
                dayPnL,
                dayPnLPercentage);
    }

    @Override
    public List<SectorAllocationResponse> getSectorAllocation() {
        List<Holding> holdings = holdingRepository.findByUserId(currentUser.getUserId()).stream()
                .filter(h -> h.getQuantity() > 0)
                .toList();
        Map<String, StockQuoteData> prices = fetchPrices();

        Map<String, BigDecimal> sectorInvested = new LinkedHashMap<>();
        Map<String, BigDecimal> sectorCurrent = new LinkedHashMap<>();
        Map<String, Integer> sectorCount = new LinkedHashMap<>();
        BigDecimal totalCurrent = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            Stock stock = holding.getStock();
            String sector = stock.getSector() != null ? stock.getSector() : "Unknown";

            sectorInvested.merge(sector, holding.getInvestedAmount(), BigDecimal::add);
            sectorCount.merge(sector, 1, Integer::sum);

            StockQuoteData quote = prices.get(stock.getSymbol());
            BigDecimal value;
            if (quote != null && quote.ltp() != null) {
                value = quote.ltp().multiply(BigDecimal.valueOf(holding.getQuantity()));
            } else {
                value = holding.getInvestedAmount();
            }
            sectorCurrent.merge(sector, value, BigDecimal::add);
            totalCurrent = totalCurrent.add(value);
        }

        List<SectorAllocationResponse> result = new ArrayList<>();
        for (String sector : sectorInvested.keySet()) {
            BigDecimal percentage = totalCurrent.compareTo(BigDecimal.ZERO) > 0
                    ? sectorCurrent.get(sector)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalCurrent, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            result.add(new SectorAllocationResponse(
                    sector,
                    sectorInvested.get(sector),
                    sectorCurrent.get(sector),
                    percentage,
                    sectorCount.get(sector)));
        }

        return result;
    }

    @Override
    public List<StockPnLResponse> getStockWisePnL() {
        List<Holding> holdings = holdingRepository.findByUserId(currentUser.getUserId());
        Map<String, StockQuoteData> prices = fetchPrices();

        List<StockPnLResponse> result = new ArrayList<>();
        for (Holding holding : holdings) {
            Stock stock = holding.getStock();
            StockQuoteData quote = prices.get(stock.getSymbol());

            BigDecimal currentPrice = quote != null ? quote.ltp() : null;
            BigDecimal currentValue = null;
            BigDecimal pnl = null;
            BigDecimal pnlPercentage = null;

            if (currentPrice != null) {
                currentValue = currentPrice.multiply(
                        BigDecimal.valueOf(holding.getQuantity()));
                pnl = CalculationUtils.calculatePnL(currentValue, holding.getInvestedAmount());
                pnlPercentage = CalculationUtils.calculatePnLPercentage(
                        pnl, holding.getInvestedAmount());
            }

            result.add(new StockPnLResponse(
                    stock.getSymbol(),
                    stock.getCompanyName(),
                    holding.getQuantity(),
                    holding.getAverageBuyPrice(),
                    currentPrice,
                    holding.getInvestedAmount(),
                    currentValue,
                    pnl,
                    pnlPercentage));
        }

        return result;
    }

    private Map<String, StockQuoteData> fetchPrices() {
        try {
            return marketDataService.getCurrentPrices();
        } catch (MarketDataException e) {
            return Map.of();
        }
    }
}
