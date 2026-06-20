package com.stocks.myportfolio.dto.request;

import java.math.BigDecimal;

import com.stocks.myportfolio.common.enums.SignalStatus;

public record UpdateTradingSignalRequest(
        SignalStatus status,
        BigDecimal targetPrice,
        BigDecimal stopLoss,
        String rationale,
        String notes) {
}
