package com.stocks.myportfolio.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.stocks.myportfolio.common.constants.AppConstants;
import com.stocks.myportfolio.common.exception.MarketDataException;
import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.dto.request.CreateHoldingRequest;
import com.stocks.myportfolio.dto.request.UpdateHoldingRequest;
import com.stocks.myportfolio.dto.response.HoldingResponse;
import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.integration.StockQuoteData;
import com.stocks.myportfolio.mapper.HoldingMapper;
import com.stocks.myportfolio.repository.HoldingRepository;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import com.stocks.myportfolio.service.HoldingService;
import com.stocks.myportfolio.service.MarketDataService;

@Service
public class HoldingServiceImpl implements HoldingService {

    private static final Logger log = LoggerFactory.getLogger(HoldingServiceImpl.class);

    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;
    private final HoldingMapper holdingMapper;
    private final MarketDataService marketDataService;
    private final CurrentUserProvider currentUser;

    public HoldingServiceImpl(
            HoldingRepository holdingRepository,
            StockRepository stockRepository,
            HoldingMapper holdingMapper,
            MarketDataService marketDataService,
            CurrentUserProvider currentUser) {

        this.holdingRepository = holdingRepository;
        this.stockRepository = stockRepository;
        this.holdingMapper = holdingMapper;
        this.marketDataService = marketDataService;
        this.currentUser = currentUser;
    }

    @Override
    public HoldingResponse createHolding(CreateHoldingRequest request) {
        Stock stock = stockRepository
                .findBySymbolIgnoreCase(request.symbol())
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.STOCK_NOT_FOUND + ": " + request.symbol()));

        BigDecimal investedAmount = request.averageBuyPrice()
                .multiply(BigDecimal.valueOf(request.quantity()));

        Holding holding = new Holding();
        holding.setStock(stock);
        holding.setQuantity(request.quantity());
        holding.setAverageBuyPrice(request.averageBuyPrice());
        holding.setInvestedAmount(investedAmount);

        Holding saved = holdingRepository.save(holding);
        return holdingMapper.toResponse(saved);
    }

    @Override
    public HoldingResponse updateHolding(Long id, UpdateHoldingRequest request) {
        Holding holding = holdingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.HOLDING_NOT_FOUND + ": " + id));

        if (request.quantity() != null) {
            holding.setQuantity(request.quantity());
        }
        if (request.averageBuyPrice() != null) {
            holding.setAverageBuyPrice(request.averageBuyPrice());
        }

        holding.setInvestedAmount(
                holding.getAverageBuyPrice()
                        .multiply(BigDecimal.valueOf(holding.getQuantity())));

        Holding saved = holdingRepository.save(holding);
        return holdingMapper.toResponse(saved);
    }

    @Override
    public List<HoldingResponse> getAllHoldings() {
        List<Holding> holdings = holdingRepository.findByUserId(currentUser.getUserId());
        Map<String, StockQuoteData> prices;

        try {
            prices = marketDataService.getCurrentPrices();
        } catch (MarketDataException e) {
            log.warn("Failed to fetch market data for holdings: {}", e.getMessage());
            prices = Map.of();
        }

        List<HoldingResponse> responses = new ArrayList<>();
        for (Holding holding : holdings) {
            StockQuoteData quote = prices.get(holding.getStock().getSymbol());
            responses.add(holdingMapper.toResponse(holding, quote));
        }

        return responses;
    }
}
