package com.stocks.myportfolio.controller;

import com.stocks.myportfolio.dto.request.CreateHoldingRequest;
import com.stocks.myportfolio.dto.response.HoldingResponse;
import com.stocks.myportfolio.service.HoldingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/holdings")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(
            HoldingService holdingService) {

        this.holdingService = holdingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HoldingResponse createHolding(
            @Valid
            @RequestBody
            CreateHoldingRequest request) {

        return holdingService
                .createHolding(request);
    }
}