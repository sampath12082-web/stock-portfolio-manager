package com.stocks.myportfolio.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocks.myportfolio.dto.response.PortfolioSummaryResponse;
import com.stocks.myportfolio.dto.response.SectorAllocationResponse;
import com.stocks.myportfolio.dto.response.StockPnLResponse;
import com.stocks.myportfolio.service.PortfolioService;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/summary")
    public PortfolioSummaryResponse getSummary() {
        return portfolioService.getPortfolioSummary();
    }

    @GetMapping("/allocation")
    public List<SectorAllocationResponse> getAllocation() {
        return portfolioService.getSectorAllocation();
    }

    @GetMapping("/pnl")
    public List<StockPnLResponse> getPnL() {
        return portfolioService.getStockWisePnL();
    }
}
