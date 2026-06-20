package com.stocks.myportfolio.service;

import java.time.LocalDate;
import java.util.List;

import com.stocks.myportfolio.dto.request.CreateTradingSignalRequest;
import com.stocks.myportfolio.dto.request.UpdateTradingSignalRequest;
import com.stocks.myportfolio.dto.response.TradingSignalResponse;

public interface TradingSignalService {

    TradingSignalResponse createSignal(CreateTradingSignalRequest request);

    TradingSignalResponse updateSignal(Long id, UpdateTradingSignalRequest request);

    List<TradingSignalResponse> getSignalsForDate(LocalDate date);

    List<TradingSignalResponse> getTodaySignals();

    List<TradingSignalResponse> getActiveSignals();

    List<TradingSignalResponse> getSignalsBySymbol(String symbol);

    void cancelSignal(Long id);

    void expireOldSignals();
}
