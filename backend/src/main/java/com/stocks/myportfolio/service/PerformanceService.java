package com.stocks.myportfolio.service;

import java.time.LocalDate;
import java.util.List;

import com.stocks.myportfolio.dto.response.PortfolioSnapshotResponse;

public interface PerformanceService {

    List<PortfolioSnapshotResponse> getPerformanceHistory(LocalDate from, LocalDate to);

    List<PortfolioSnapshotResponse> getRecentPerformance(int days);

    PortfolioSnapshotResponse getTodaySnapshot();

    PortfolioSnapshotResponse captureSnapshot();
}
