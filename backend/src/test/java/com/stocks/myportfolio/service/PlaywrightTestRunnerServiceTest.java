package com.stocks.myportfolio.service;

import com.stocks.myportfolio.dto.response.TestRunResult;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlaywrightTestRunnerServiceTest {

    private final PlaywrightTestRunnerService runner = new PlaywrightTestRunnerService();

    @Test
    void invalidSuite_returnsFailure() {
        TestRunResult result = runner.runTestSuite("hacker; rm -rf /");
        assertFalse(result.success());
        assertTrue(result.errorMessages().get(0).contains("Invalid suite"));
    }

    @Test
    void validSuiteNames() {
        Set<String> valid = Set.of("smoke", "functional", "regression", "auth", "ui-rendering");
        for (String name : valid) {
            // Just verify it doesn't reject valid names as invalid
            // Actual execution may fail if e2e dir not configured
            TestRunResult result = runner.runTestSuite(name);
            assertTrue(result.errorMessages().isEmpty() || !result.errorMessages().get(0).contains("Invalid suite"),
                    name + " should not be rejected as invalid");
        }
    }
}
