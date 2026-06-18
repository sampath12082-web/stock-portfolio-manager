package com.stocks.myportfolio.controller;

import com.stocks.myportfolio.dto.request.CreateStockRequest;
import com.stocks.myportfolio.dto.response.StockResponse;
import com.stocks.myportfolio.service.StockService;

import jakarta.validation.Valid;

import java.util.List;

// import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks")
// @RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponse createStock(
            @Valid @RequestBody CreateStockRequest request) {

        return stockService.createStock(request);
    }

    @GetMapping("/{symbol}")
    public StockResponse getStockBySymbol(
            @PathVariable String symbol) {

        return stockService.getStockBySymbol(symbol);
    }

    @GetMapping
    public List<StockResponse> getAllStocks() {
        return stockService.getAllStocks();
    }
}