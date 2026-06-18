package com.stocks.myportfolio.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.stocks.myportfolio.dto.request.CreateStockRequest;
import com.stocks.myportfolio.dto.response.StockResponse;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.service.StockService;

@Service
public class StockServiceImpl implements StockService {

        private final StockRepository stockRepository;

        public StockServiceImpl(StockRepository stockRepository) {
                this.stockRepository = stockRepository;
        }

        @Override
        public StockResponse createStock(CreateStockRequest request) {

                Optional<Stock> existingStock = stockRepository.findBySymbol(request.symbol());

                if (existingStock.isPresent()) {
                        throw new RuntimeException(
                                        "Stock already exists: " + request.symbol());
                }

                Stock stock = new Stock();

                stock.setSymbol(request.symbol().toUpperCase());
                stock.setCompanyName(request.companyName());
                stock.setExchange(request.exchange());
                stock.setSector(request.sector());
                stock.setIndustry(request.industry());

                Stock savedStock = stockRepository.save(stock);

                return new StockResponse(
                                savedStock.getId(),
                                savedStock.getSymbol(),
                                savedStock.getCompanyName(),
                                savedStock.getExchange(),
                                savedStock.getSector(),
                                savedStock.getIndustry());
        }

        @Override
        public StockResponse getStockBySymbol(String symbol) {

                Stock stock = stockRepository
                                .findBySymbolIgnoreCase(symbol)
                                .orElseThrow(() -> new RuntimeException(
                                                "Stock not found: " + symbol));

                return new StockResponse(
                                stock.getId(),
                                stock.getSymbol(),
                                stock.getCompanyName(),
                                stock.getExchange(),
                                stock.getSector(),
                                stock.getIndustry());
        }

        @Override
        public List<StockResponse> getAllStocks() {

                return stockRepository.findAll()
                                .stream()
                                .map(stock -> new StockResponse(
                                                stock.getId(),
                                                stock.getSymbol(),
                                                stock.getCompanyName(),
                                                stock.getExchange(),
                                                stock.getSector(),
                                                stock.getIndustry()))
                                .toList();
        }
}