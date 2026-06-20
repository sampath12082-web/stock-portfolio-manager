package com.stocks.myportfolio.service;

import java.util.List;

import com.stocks.myportfolio.dto.response.TradingSignalResponse;

public interface TechnicalAnalysisService {

    List<TradingSignalResponse> analyzeHoldings();

    List<TradingSignalResponse> scanMarket();

    void runDailyAnalysis();
}
