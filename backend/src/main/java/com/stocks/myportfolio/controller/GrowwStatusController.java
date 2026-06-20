package com.stocks.myportfolio.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groww")
public class GrowwStatusController {

    @Value("${groww.api.enabled:false}")
    private boolean growwEnabled;

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return Map.of(
                "enabled", growwEnabled,
                "message", growwEnabled
                        ? "Groww API is configured"
                        : "Groww API not configured. Set GROWW_API_ENABLED=true environment variable.");
    }
}
