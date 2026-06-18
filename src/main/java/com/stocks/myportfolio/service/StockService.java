package com.stocks.myportfolio.service;

import java.util.List;

import com.stocks.myportfolio.dto.request.CreateStockRequest;
import com.stocks.myportfolio.dto.response.StockResponse;

public interface StockService {

    StockResponse createStock(CreateStockRequest request);

    StockResponse getStockBySymbol(String symbol);

    List<StockResponse> getAllStocks();

}