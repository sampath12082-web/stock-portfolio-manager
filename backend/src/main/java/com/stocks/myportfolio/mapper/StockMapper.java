package com.stocks.myportfolio.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.stocks.myportfolio.dto.request.CreateStockRequest;
import com.stocks.myportfolio.dto.response.StockResponse;
import com.stocks.myportfolio.entity.Stock;

@Component
public class StockMapper {

    public Stock toEntity(CreateStockRequest request) {
        Stock stock = new Stock();
        stock.setSymbol(request.symbol().toUpperCase());
        stock.setCompanyName(request.companyName());
        stock.setExchange(request.exchange());
        stock.setSector(request.sector());
        stock.setIndustry(request.industry());
        return stock;
    }

    public StockResponse toResponse(Stock stock) {
        return new StockResponse(
                stock.getId(),
                stock.getSymbol(),
                stock.getCompanyName(),
                stock.getExchange(),
                stock.getSector(),
                stock.getIndustry());
    }

    public List<StockResponse> toResponseList(List<Stock> stocks) {
        return stocks.stream()
                .map(this::toResponse)
                .toList();
    }
}
