package com.stocks.myportfolio.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record GrowwAccountResponse(
        String userId,
        String ucc,
        boolean nseEnabled,
        boolean bseEnabled,
        List<String> activeSegments,
        BigDecimal availableCash,
        BigDecimal clearCash,
        BigDecimal marginUsed,
        List<GrowwPositionData> todayPositions,
        List<GrowwOrderSummary> todayOrders) {

    public record GrowwPositionData(
            String symbol,
            String exchange,
            int quantity,
            BigDecimal netPrice,
            BigDecimal realisedPnl,
            String product) {
    }

    public record GrowwOrderSummary(
            String symbol,
            String transactionType,
            String status,
            int quantity,
            int filledQuantity,
            BigDecimal price,
            BigDecimal avgFillPrice,
            String exchange,
            String tradeDate) {
    }
}
