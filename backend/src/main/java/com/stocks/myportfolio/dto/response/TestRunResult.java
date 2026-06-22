package com.stocks.myportfolio.dto.response;

import java.util.List;

public record TestRunResult(
        String suiteName,
        int totalTests,
        int passed,
        int failed,
        int skipped,
        List<String> failedTests,
        List<String> errorMessages,
        long durationMs,
        boolean success
) {}
