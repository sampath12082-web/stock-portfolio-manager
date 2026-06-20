package com.stocks.myportfolio.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.dto.response.PortfolioSnapshotResponse;
import com.stocks.myportfolio.service.PerformanceService;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping("/history")
    public List<PortfolioSnapshotResponse> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return performanceService.getPerformanceHistory(from, to);
    }

    @GetMapping("/recent")
    public List<PortfolioSnapshotResponse> getRecent(
            @RequestParam(defaultValue = "30") int days) {

        return performanceService.getRecentPerformance(days);
    }

    @GetMapping("/today")
    public PortfolioSnapshotResponse getToday() {
        return performanceService.getTodaySnapshot();
    }

    @PostMapping("/snapshot")
    public PortfolioSnapshotResponse captureSnapshot() {
        return performanceService.captureSnapshot();
    }
}
