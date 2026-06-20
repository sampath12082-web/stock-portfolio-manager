package com.stocks.myportfolio.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record TransactionAnalyticsResponse(
        BigDecimal totalBuyAmount,
        BigDecimal totalSellAmount,
        BigDecimal realizedGains,
        int totalTransactions,
        int buyCount,
        int sellCount,
        String mostTradedStock,
        Map<String, Integer> transactionsByMonth,
        BigDecimal totalDeposits,
        BigDecimal totalWithdrawals,
        BigDecimal totalCharges,
        BigDecimal intradayBuyAmount,
        BigDecimal intradaySellAmount,
        BigDecimal intradayPnL,
        BigDecimal deliveryBuyAmount,
        BigDecimal deliverySellAmount,
        BigDecimal deliveryRealizedGains,
        int intradayCount,
        int deliveryCount) {
}
