package com.stocks.myportfolio.service;

import java.util.List;

import com.stocks.myportfolio.dto.response.PortfolioSummaryResponse;
import com.stocks.myportfolio.dto.response.SectorAllocationResponse;
import com.stocks.myportfolio.dto.response.StockPnLResponse;

public interface PortfolioService {

    PortfolioSummaryResponse getPortfolioSummary();

    List<SectorAllocationResponse> getSectorAllocation();

    List<StockPnLResponse> getStockWisePnL();
}
