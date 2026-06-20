package com.stocks.myportfolio.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocks.myportfolio.dto.response.GrowwAccountResponse;
import com.stocks.myportfolio.dto.response.GrowwSyncResponse;
import com.stocks.myportfolio.service.GrowwSyncService;

@RestController
@RequestMapping("/api/groww")
@ConditionalOnProperty(name = "groww.api.enabled", havingValue = "true")
public class GrowwSyncController {

    private final GrowwSyncService growwSyncService;

    public GrowwSyncController(GrowwSyncService growwSyncService) {
        this.growwSyncService = growwSyncService;
    }

    @PostMapping("/sync")
    public GrowwSyncResponse syncPortfolio() {
        return growwSyncService.syncPortfolio();
    }

    @PostMapping("/sync-orders")
    public GrowwSyncResponse syncOrders() {
        return growwSyncService.syncOrders();
    }

    @GetMapping("/account")
    public GrowwAccountResponse getAccountDetails() {
        return growwSyncService.getAccountDetails();
    }
}
