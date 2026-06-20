package com.stocks.myportfolio.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CalculationUtils {

    private CalculationUtils() {
    }

    public static BigDecimal calculatePnL(
            BigDecimal currentValue,
            BigDecimal investedAmount) {

        return currentValue.subtract(investedAmount);
    }

    public static BigDecimal calculatePnLPercentage(
            BigDecimal pnl,
            BigDecimal investedAmount) {

        if (investedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return pnl.multiply(BigDecimal.valueOf(100))
                .divide(
                        investedAmount,
                        2,
                        RoundingMode.HALF_UP);
    }
}