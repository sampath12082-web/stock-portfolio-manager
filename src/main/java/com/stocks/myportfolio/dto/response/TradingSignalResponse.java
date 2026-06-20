package com.stocks.myportfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.stocks.myportfolio.common.enums.SignalStatus;
import com.stocks.myportfolio.common.enums.SignalType;

public record TradingSignalResponse(
        Long id,
        String symbol,
        String companyName,
        SignalType signalType,
        BigDecimal targetPrice,
        BigDecimal stopLoss,
        BigDecimal currentPrice,
        String rationale,
        LocalDate signalDate,
        SignalStatus status,
        String notes,
        LocalDateTime createdAt) {
}
