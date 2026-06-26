package com.stocks.myportfolio.service;

import com.stocks.myportfolio.dto.response.DashboardResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Transaction;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.repository.TransactionRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import com.stocks.myportfolio.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stocks.myportfolio.common.enums.Exchange;
import com.stocks.myportfolio.entity.Stock;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private HoldingRepository holdingRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private MarketDataService marketDataService;
    @Mock private CurrentUserProvider currentUser;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setup() {
        dashboardService = new DashboardServiceImpl(holdingRepository, transactionRepository, marketDataService, currentUser);
        when(currentUser.getUserId()).thenReturn(1L);
    }

    @Test
    void getDashboard_emptyHoldings_returnsZeros() {
        when(holdingRepository.findByUserId(1L)).thenReturn(List.of());
        when(transactionRepository.findByUserId(1L)).thenReturn(List.of());
        when(marketDataService.getCurrentPrices()).thenReturn(java.util.Map.of());

        DashboardResponse resp = dashboardService.getDashboard();
        assertEquals(BigDecimal.ZERO, resp.investedAmount());
        assertEquals(BigDecimal.ZERO, resp.currentValue());
        assertEquals(BigDecimal.ZERO, resp.unrealizedPnL());
    }

    private Holding createHolding(String symbol, int qty, double invested) {
        Stock stock = new Stock();
        stock.setSymbol(symbol);
        stock.setExchange(Exchange.NSE);
        Holding h = new Holding();
        h.setStock(stock);
        h.setQuantity(qty);
        h.setAverageBuyPrice(BigDecimal.valueOf(invested / Math.max(qty, 1)));
        h.setInvestedAmount(BigDecimal.valueOf(invested));
        return h;
    }

    @Test
    void getDashboard_withActiveHoldings_calculatesInvested() {
        Holding h = createHolding("TCS", 10, 1000);
        when(holdingRepository.findByUserId(1L)).thenReturn(List.of(h));
        when(transactionRepository.findByUserId(1L)).thenReturn(List.of());
        when(marketDataService.getCurrentPrices()).thenReturn(java.util.Map.of());

        DashboardResponse resp = dashboardService.getDashboard();
        assertEquals(BigDecimal.valueOf(1000.0), resp.investedAmount());
    }

    @Test
    void getDashboard_zeroQuantityHoldings_excluded() {
        Holding active = createHolding("TCS", 5, 500);
        Holding sold = createHolding("INFY", 0, 300);
        when(holdingRepository.findByUserId(1L)).thenReturn(List.of(active, sold));
        when(transactionRepository.findByUserId(1L)).thenReturn(List.of());
        when(marketDataService.getCurrentPrices()).thenReturn(java.util.Map.of());

        DashboardResponse resp = dashboardService.getDashboard();
        assertEquals(BigDecimal.valueOf(500.0), resp.investedAmount());
    }

    @Test
    void getDashboard_unrealizedPnL_isCurrentMinusInvested() {
        Holding h = createHolding("TCS", 10, 1000);
        when(holdingRepository.findByUserId(1L)).thenReturn(List.of(h));
        when(transactionRepository.findByUserId(1L)).thenReturn(List.of());
        when(marketDataService.getCurrentPrices()).thenReturn(java.util.Map.of());

        DashboardResponse resp = dashboardService.getDashboard();
        BigDecimal expected = resp.currentValue().subtract(resp.investedAmount());
        assertEquals(0, expected.compareTo(resp.unrealizedPnL()));
    }
}
