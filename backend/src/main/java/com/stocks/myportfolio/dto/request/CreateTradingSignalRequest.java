package com.stocks.myportfolio.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.stocks.myportfolio.common.enums.SignalType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTradingSignalRequest(
        @NotBlank String symbol,
        @NotNull SignalType signalType,
        BigDecimal targetPrice,
        BigDecimal stopLoss,
        String rationale,
        LocalDate signalDate,
        String notes) {
}
