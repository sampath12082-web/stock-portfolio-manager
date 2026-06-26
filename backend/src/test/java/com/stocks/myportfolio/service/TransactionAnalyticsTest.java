package com.stocks.myportfolio.service;

import com.stocks.myportfolio.common.enums.TradeType;
import com.stocks.myportfolio.common.enums.TransactionType;
import com.stocks.myportfolio.dto.response.TransactionAnalyticsResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Transaction;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.repository.TransactionRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import com.stocks.myportfolio.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionAnalyticsTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private StockRepository stockRepository;
    @Mock private HoldingRepository holdingRepository;
    @Mock private CurrentUserProvider currentUser;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setup() {
        transactionService = new TransactionServiceImpl(transactionRepository, stockRepository, holdingRepository, currentUser);
        lenient().when(currentUser.getUserId()).thenReturn(1L);
    }

    @Test
    void analytics_emptyTransactions_allZeros() {
        when(transactionRepository.findByUserId(1L)).thenReturn(List.of());
        TransactionAnalyticsResponse a = transactionService.getAnalytics();
        assertEquals(0, a.totalTransactions());
        assertEquals(BigDecimal.ZERO, a.totalBuyAmount());
        assertEquals(BigDecimal.ZERO, a.totalSellAmount());
    }

    @Test
    void analytics_countsTransactions() {
        Transaction buy = createTxn(TransactionType.BUY, 100, "CNC");
        Transaction sell = createTxn(TransactionType.SELL, 50, "CNC");
        Transaction deposit = createTxn(TransactionType.DEPOSIT, 1000, null);
        when(transactionRepository.findByUserId(1L)).thenReturn(List.of(buy, sell, deposit));

        TransactionAnalyticsResponse a = transactionService.getAnalytics();
        assertEquals(3, a.totalTransactions());
    }

    @Test
    void analytics_separatesCncMis() {
        Transaction cncBuy = createTxn(TransactionType.BUY, 100, "CNC");
        Transaction misBuy = createTxn(TransactionType.BUY, 200, "MIS");
        when(transactionRepository.findByUserId(1L)).thenReturn(List.of(cncBuy, misBuy));

        TransactionAnalyticsResponse a = transactionService.getAnalytics();
        assertEquals(0, BigDecimal.valueOf(100).compareTo(a.deliveryBuyAmount()));
        assertEquals(0, BigDecimal.valueOf(200).compareTo(a.intradayBuyAmount()));
    }

    private Transaction createTxn(TransactionType type, double amount, String tradeTypeStr) {
        Transaction t = new Transaction();
        t.setTransactionType(type);
        t.setTotalAmount(BigDecimal.valueOf(amount));
        t.setQuantity(1);
        t.setPrice(BigDecimal.valueOf(amount));
        if (tradeTypeStr != null) t.setTradeType(TradeType.valueOf(tradeTypeStr));
        return t;
    }
}
