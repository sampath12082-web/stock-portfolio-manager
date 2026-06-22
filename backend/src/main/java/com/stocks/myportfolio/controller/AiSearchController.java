package com.stocks.myportfolio.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.service.AiStockService;

@RestController
@RequestMapping("/api/ai")
public class AiSearchController {

    private final AiStockService aiStockService;

    public AiSearchController(AiStockService aiStockService) {
        this.aiStockService = aiStockService;
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String query) {
        return aiStockService.searchAndAnalyze(query);
    }
}
