package com.stocks.myportfolio.dto.request;

import com.stocks.myportfolio.common.enums.Exchange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStockRequest(

        @NotBlank
        String symbol,

        @NotBlank
        String companyName,

        @NotNull
        Exchange exchange,

        String sector,

        String industry
) {
}