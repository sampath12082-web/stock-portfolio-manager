package com.stocks.myportfolio.service;

import java.util.List;

import com.stocks.myportfolio.dto.request.CreateHoldingRequest;
import com.stocks.myportfolio.dto.request.UpdateHoldingRequest;
import com.stocks.myportfolio.dto.response.HoldingResponse;

public interface HoldingService {

    HoldingResponse createHolding(CreateHoldingRequest request);

    List<HoldingResponse> getAllHoldings();

    HoldingResponse updateHolding(Long id, UpdateHoldingRequest request);
}
