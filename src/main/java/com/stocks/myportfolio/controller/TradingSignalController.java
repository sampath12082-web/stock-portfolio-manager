package com.stocks.myportfolio.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.dto.request.CreateTradingSignalRequest;
import com.stocks.myportfolio.dto.request.UpdateTradingSignalRequest;
import com.stocks.myportfolio.dto.response.TradingSignalResponse;
import com.stocks.myportfolio.service.TechnicalAnalysisService;
import com.stocks.myportfolio.service.TradingSignalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/signals")
public class TradingSignalController {

    private final TradingSignalService tradingSignalService;
    private final TechnicalAnalysisService technicalAnalysisService;

    public TradingSignalController(
            TradingSignalService tradingSignalService,
            TechnicalAnalysisService technicalAnalysisService) {

        this.tradingSignalService = tradingSignalService;
        this.technicalAnalysisService = technicalAnalysisService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TradingSignalResponse createSignal(
            @Valid @RequestBody CreateTradingSignalRequest request) {

        return tradingSignalService.createSignal(request);
    }

    @PutMapping("/{id}")
    public TradingSignalResponse updateSignal(
            @PathVariable Long id,
            @RequestBody UpdateTradingSignalRequest request) {

        return tradingSignalService.updateSignal(id, request);
    }

    @GetMapping("/today")
    public List<TradingSignalResponse> getTodaySignals() {
        return tradingSignalService.getTodaySignals();
    }

    @GetMapping("/active")
    public List<TradingSignalResponse> getActiveSignals() {
        return tradingSignalService.getActiveSignals();
    }

    @GetMapping
    public List<TradingSignalResponse> getSignals(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String symbol) {

        if (symbol != null) {
            return tradingSignalService.getSignalsBySymbol(symbol);
        }
        if (date != null) {
            return tradingSignalService.getSignalsForDate(date);
        }
        return tradingSignalService.getActiveSignals();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSignal(@PathVariable Long id) {
        tradingSignalService.cancelSignal(id);
    }

    @PostMapping("/analyze")
    public List<TradingSignalResponse> analyzeHoldings() {
        return technicalAnalysisService.analyzeHoldings();
    }

    @GetMapping("/recommendations")
    public List<TradingSignalResponse> getRecommendations() {
        return technicalAnalysisService.scanMarket();
    }
}
