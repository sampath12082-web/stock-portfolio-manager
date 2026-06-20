package com.stocks.myportfolio.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

public record UpdateHoldingRequest(
        @Min(1) Integer quantity,
        @DecimalMin("0.01") BigDecimal averageBuyPrice) {
}
