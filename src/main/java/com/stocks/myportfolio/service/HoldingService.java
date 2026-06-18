package com.stocks.myportfolio.service;

import com.stocks.myportfolio.dto.request.CreateHoldingRequest;
import com.stocks.myportfolio.dto.response.HoldingResponse;

public interface HoldingService {

    HoldingResponse createHolding(
            CreateHoldingRequest request);
}