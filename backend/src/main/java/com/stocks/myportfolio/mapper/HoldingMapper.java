package com.stocks.myportfolio.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.stocks.myportfolio.common.util.CalculationUtils;
import com.stocks.myportfolio.dto.response.HoldingResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.integration.StockQuoteData;

@Component
public class HoldingMapper {

    public HoldingResponse toResponse(Holding holding, StockQuoteData quote) {
        BigDecimal currentPrice = quote != null ? quote.ltp() : null;
        BigDecimal currentValue = null;
        BigDecimal pnl = null;
        BigDecimal pnlPercentage = null;
        BigDecimal dayChange = null;
        BigDecimal dayChangePercentage = null;

        if (currentPrice != null) {
            currentValue = currentPrice.multiply(BigDecimal.valueOf(holding.getQuantity()));
            pnl = CalculationUtils.calculatePnL(currentValue, holding.getInvestedAmount());
            pnlPercentage = CalculationUtils.calculatePnLPercentage(pnl, holding.getInvestedAmount());

            if (quote.previousClose() != null) {
                BigDecimal previousValue = quote.previousClose()
                        .multiply(BigDecimal.valueOf(holding.getQuantity()));
                dayChange = CalculationUtils.calculatePnL(currentValue, previousValue);
                dayChangePercentage = CalculationUtils.calculatePnLPercentage(dayChange, previousValue);
            }
        }

        return new HoldingResponse(
                holding.getId(),
                holding.getStock().getSymbol(),
                holding.getQuantity(),
                holding.getAverageBuyPrice(),
                holding.getInvestedAmount(),
                currentPrice,
                currentValue,
                pnl,
                pnlPercentage,
                dayChange,
                dayChangePercentage);
    }

    public HoldingResponse toResponse(Holding holding) {
        return toResponse(holding, null);
    }

    public List<HoldingResponse> toResponseList(List<Holding> holdings) {
        return holdings.stream()
                .map(this::toResponse)
                .toList();
    }
}
