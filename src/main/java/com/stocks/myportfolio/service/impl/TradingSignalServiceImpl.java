package com.stocks.myportfolio.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocks.myportfolio.common.enums.SignalStatus;
import com.stocks.myportfolio.common.exception.MarketDataException;
import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.dto.request.CreateTradingSignalRequest;
import com.stocks.myportfolio.dto.request.UpdateTradingSignalRequest;
import com.stocks.myportfolio.dto.response.TradingSignalResponse;
import com.stocks.myportfolio.entity.TradingSignal;
import com.stocks.myportfolio.integration.StockQuoteData;
import com.stocks.myportfolio.repository.StockRepository;
import com.stocks.myportfolio.repository.TradingSignalRepository;
import com.stocks.myportfolio.service.MarketDataService;
import com.stocks.myportfolio.service.TradingSignalService;

@Service
@Transactional
public class TradingSignalServiceImpl implements TradingSignalService {

    private final TradingSignalRepository signalRepository;
    private final StockRepository stockRepository;
    private final MarketDataService marketDataService;

    public TradingSignalServiceImpl(
            TradingSignalRepository signalRepository,
            StockRepository stockRepository,
            MarketDataService marketDataService) {

        this.signalRepository = signalRepository;
        this.stockRepository = stockRepository;
        this.marketDataService = marketDataService;
    }

    @Override
    public TradingSignalResponse createSignal(CreateTradingSignalRequest request) {
        TradingSignal signal = new TradingSignal();
        signal.setSymbol(request.symbol().toUpperCase());
        signal.setSignalType(request.signalType());
        signal.setTargetPrice(request.targetPrice());
        signal.setStopLoss(request.stopLoss());
        signal.setRationale(request.rationale());
        signal.setSignalDate(request.signalDate() != null
                ? request.signalDate() : LocalDate.now());
        signal.setStatus(SignalStatus.ACTIVE);
        signal.setNotes(request.notes());

        stockRepository.findBySymbolIgnoreCase(request.symbol())
                .ifPresent(stock -> {
                    signal.setStock(stock);
                    try {
                        StockQuoteData quote = marketDataService.getCurrentPrice(
                                stock.getSymbol());
                        if (quote != null && quote.ltp() != null) {
                            signal.setCurrentPrice(quote.ltp());
                        }
                    } catch (MarketDataException e) {
                        // Market data unavailable — leave currentPrice null
                    }
                });

        TradingSignal saved = signalRepository.save(signal);
        return toResponse(saved);
    }

    @Override
    public TradingSignalResponse updateSignal(
            Long id, UpdateTradingSignalRequest request) {

        TradingSignal signal = signalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trading signal not found: " + id));

        if (request.status() != null) {
            signal.setStatus(request.status());
        }
        if (request.targetPrice() != null) {
            signal.setTargetPrice(request.targetPrice());
        }
        if (request.stopLoss() != null) {
            signal.setStopLoss(request.stopLoss());
        }
        if (request.rationale() != null) {
            signal.setRationale(request.rationale());
        }
        if (request.notes() != null) {
            signal.setNotes(request.notes());
        }

        TradingSignal saved = signalRepository.save(signal);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradingSignalResponse> getSignalsForDate(LocalDate date) {
        return signalRepository.findBySignalDateOrderByCreatedAtDesc(date)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradingSignalResponse> getTodaySignals() {
        return getSignalsForDate(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradingSignalResponse> getActiveSignals() {
        return signalRepository.findByStatusOrderBySignalDateDesc(SignalStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradingSignalResponse> getSignalsBySymbol(String symbol) {
        return signalRepository.findBySymbolIgnoreCaseOrderBySignalDateDesc(symbol)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void cancelSignal(Long id) {
        TradingSignal signal = signalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trading signal not found: " + id));

        signal.setStatus(SignalStatus.CANCELLED);
        signalRepository.save(signal);
    }

    @Override
    public void expireOldSignals() {
        LocalDate cutoff = LocalDate.now().minusDays(7);
        List<TradingSignal> expired = signalRepository
                .findByStatusAndSignalDateBefore(SignalStatus.ACTIVE, cutoff);

        for (TradingSignal signal : expired) {
            signal.setStatus(SignalStatus.EXPIRED);
        }
        signalRepository.saveAll(expired);
    }

    private TradingSignalResponse toResponse(TradingSignal signal) {
        String companyName = null;
        if (signal.getStock() != null) {
            companyName = signal.getStock().getCompanyName();
        }

        return new TradingSignalResponse(
                signal.getId(),
                signal.getSymbol(),
                companyName,
                signal.getSignalType(),
                signal.getTargetPrice(),
                signal.getStopLoss(),
                signal.getCurrentPrice(),
                signal.getRationale(),
                signal.getSignalDate(),
                signal.getStatus(),
                signal.getNotes(),
                signal.getCreatedAt());
    }
}
