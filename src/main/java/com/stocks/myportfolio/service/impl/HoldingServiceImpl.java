package com.stocks.myportfolio.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.stocks.myportfolio.dto.request.CreateHoldingRequest;
import com.stocks.myportfolio.dto.response.HoldingResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.service.HoldingService;

@Service
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;

    public HoldingServiceImpl(
            HoldingRepository holdingRepository,
            StockRepository stockRepository) {

        this.holdingRepository = holdingRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    public HoldingResponse createHolding(
            CreateHoldingRequest request) {

        Stock stock = stockRepository
                .findBySymbolIgnoreCase(request.symbol())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Stock not found: "
                                        + request.symbol()));

        BigDecimal investedAmount =
                request.averageBuyPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        request.quantity()));

        Holding holding = new Holding();

        holding.setStock(stock);
        holding.setQuantity(
                BigDecimal.valueOf(
                        request.quantity()));
        holding.setAverageBuyPrice(
                request.averageBuyPrice());
        holding.setInvestedAmount(investedAmount);

        Holding saved =
                holdingRepository.save(holding);

        return new HoldingResponse(
                saved.getId(),
                stock.getSymbol(),
                saved.getQuantity().intValue(),
                saved.getAverageBuyPrice(),
                saved.getInvestedAmount()
        );
    }
}
