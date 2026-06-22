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

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> body) {
        return aiStockService.chat(body.getOrDefault("prompt", ""));
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String query) {
        return aiStockService.chat(query);
    }
}
