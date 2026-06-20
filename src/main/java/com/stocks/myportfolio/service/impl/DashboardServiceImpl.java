package com.stocks.myportfolio.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.stocks.myportfolio.common.enums.TransactionType;
import com.stocks.myportfolio.common.exception.MarketDataException;
import com.stocks.myportfolio.common.util.CalculationUtils;
import com.stocks.myportfolio.dto.response.DashboardResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Transaction;
import com.stocks.myportfolio.integration.StockQuoteData;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.repository.TransactionRepository;
import com.stocks.myportfolio.service.DashboardService;
import com.stocks.myportfolio.service.MarketDataService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final MarketDataService marketDataService;

    public DashboardServiceImpl(
            HoldingRepository holdingRepository,
            TransactionRepository transactionRepository,
            MarketDataService marketDataService) {

        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
        this.marketDataService = marketDataService;
    }

    @Override
    public DashboardResponse getDashboard() {
        List<Holding> activeHoldings = holdingRepository.findAll().stream()
                .filter(h -> h.getQuantity() > 0)
                .toList();

        BigDecimal totalInvestment = activeHoldings.stream()
                .map(Holding::getInvestedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, StockQuoteData> prices;
        try {
            prices = marketDataService.getCurrentPrices();
        } catch (MarketDataException e) {
            log.warn("Failed to fetch market data: {}", e.getMessage());
            prices = Map.of();
        }

        BigDecimal currentValue = BigDecimal.ZERO;
        for (Holding holding : activeHoldings) {
            StockQuoteData quote = prices.get(holding.getStock().getSymbol());
            if (quote != null && quote.ltp() != null) {
                currentValue = currentValue.add(
                        quote.ltp().multiply(BigDecimal.valueOf(holding.getQuantity())));
            } else {
                currentValue = currentValue.add(holding.getInvestedAmount());
            }
        }

        BigDecimal unrealizedPnL = CalculationUtils.calculatePnL(currentValue, totalInvestment);
        BigDecimal unrealizedPnLPercentage = CalculationUtils.calculatePnLPercentage(
                unrealizedPnL, totalInvestment);

        BigDecimal totalDeposited = transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionType() == TransactionType.DEPOSIT)
                .map(Transaction::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardResponse(
                totalInvestment,
                currentValue,
                unrealizedPnL,
                unrealizedPnLPercentage,
                totalDeposited);
    }
}
