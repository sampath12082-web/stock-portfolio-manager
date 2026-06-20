package com.stocks.myportfolio.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.stocks.myportfolio.service.MarketDataService;
import com.stocks.myportfolio.service.PerformanceService;
import com.stocks.myportfolio.service.TechnicalAnalysisService;
import com.stocks.myportfolio.service.TradingSignalService;

@Component
public class PortfolioScheduler {

    private static final Logger log = LoggerFactory.getLogger(PortfolioScheduler.class);

    private final PerformanceService performanceService;
    private final MarketDataService marketDataService;
    private final TradingSignalService tradingSignalService;
    private final TechnicalAnalysisService technicalAnalysisService;

    public PortfolioScheduler(
            PerformanceService performanceService,
            MarketDataService marketDataService,
            TradingSignalService tradingSignalService,
            TechnicalAnalysisService technicalAnalysisService) {

        this.performanceService = performanceService;
        this.marketDataService = marketDataService;
        this.tradingSignalService = tradingSignalService;
        this.technicalAnalysisService = technicalAnalysisService;
    }

    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void morningQuoteRefresh() {
        log.info("Running morning quote refresh");
        try {
            marketDataService.refreshAllQuotes();
        } catch (Exception e) {
            log.warn("Morning quote refresh failed: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 30 15 * * MON-FRI")
    public void dailySnapshot() {
        log.info("Capturing daily portfolio snapshot");
        try {
            performanceService.captureSnapshot();
        } catch (Exception e) {
            log.error("Daily snapshot failed: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 16 * * MON-FRI")
    public void dailyTechnicalAnalysis() {
        log.info("Running daily technical analysis");
        try {
            technicalAnalysisService.runDailyAnalysis();
        } catch (Exception e) {
            log.error("Technical analysis failed: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void expireOldSignals() {
        log.info("Expiring old trading signals");
        try {
            tradingSignalService.expireOldSignals();
        } catch (Exception e) {
            log.error("Signal expiry failed: {}", e.getMessage());
        }
    }
}
