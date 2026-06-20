package com.stocks.myportfolio.controller;

import com.stocks.myportfolio.common.enums.Exchange;
import com.stocks.myportfolio.dto.request.CreateStockRequest;
import com.stocks.myportfolio.dto.response.StockLookupResponse;
import com.stocks.myportfolio.dto.response.StockResponse;
import com.stocks.myportfolio.service.StockLookupService;
import com.stocks.myportfolio.service.StockService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;
    private final StockLookupService stockLookupService;

    public StockController(StockService stockService,
                           StockLookupService stockLookupService) {
        this.stockService = stockService;
        this.stockLookupService = stockLookupService;
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
    }

    @GetMapping("/lookup")
    public List<StockLookupResponse> lookupStocks(
            @RequestParam String query) {

        return stockLookupService.lookup(query);
    }

    @PostMapping("/refresh-sectors")
    public java.util.Map<String, Object> refreshSectors() {
        int updated = stockService.refreshSectorData();
        return java.util.Map.of("updated", updated);
    }

    @GetMapping("/search")
    public List<StockResponse> searchStocks(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Exchange exchange,
            @RequestParam(required = false) String sector) {

        return stockService.searchStocks(query, exchange, sector);
    }
}