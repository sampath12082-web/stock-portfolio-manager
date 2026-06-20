package com.stocks.myportfolio.service;

import java.util.List;

import com.stocks.myportfolio.dto.response.StockLookupResponse;

public interface StockLookupService {

    List<StockLookupResponse> lookup(String query);
}
