package com.stocks.myportfolio.service;

import java.util.List;

import com.stocks.myportfolio.common.enums.Exchange;
import com.stocks.myportfolio.dto.request.CreateStockRequest;
import com.stocks.myportfolio.dto.response.StockResponse;

public interface StockService {

    StockResponse createStock(CreateStockRequest request);

    StockResponse getStockBySymbol(String symbol);

    List<StockResponse> getAllStocks();

    List<StockResponse> searchStocks(String query, Exchange exchange, String sector);

    void deleteStock(Long id);

    int refreshSectorData();
}
