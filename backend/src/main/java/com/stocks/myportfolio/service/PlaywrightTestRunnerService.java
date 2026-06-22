package com.stocks.myportfolio.service;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocks.myportfolio.dto.response.TestRunResult;

@Service
public class PlaywrightTestRunnerService {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightTestRunnerService.class);
    private static final Set<String> VALID_SUITES = Set.of("smoke", "functional", "regression", "auth", "ui-rendering");

    @Value("${e2e.test-dir:../e2e}")
    private String e2eDir;

    @Value("${e2e.test-timeout:120}")
    private int timeoutSeconds;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TestRunResult runTestSuite(String suiteName) {
        if (!VALID_SUITES.contains(suiteName)) {
            return new TestRunResult(suiteName, 0, 0, 0, 0, List.of(), List.of("Invalid suite: " + suiteName), 0, false);
        }

        log.info("Running Playwright suite: {}", suiteName);
        long start = System.currentTimeMillis();

        try {
            List<String> commands;
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
            String testFile = "tests/" + suiteName + ".spec.ts";

            if (isWindows) {
                commands = List.of("cmd.exe", "/c", "npx", "playwright", "test", testFile, "--reporter=json");
            } else {
                commands = List.of("npx", "playwright", "test", testFile, "--reporter=json");
            }

            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.directory(new File(e2eDir));
            pb.redirectErrorStream(true);
            pb.environment().put("CI", "true");

            Process process = pb.start();
            byte[] outputBytes = process.getInputStream().readAllBytes();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return new TestRunResult(suiteName, 0, 0, 0, 0, List.of(), List.of("Timeout after " + timeoutSeconds + "s"), System.currentTimeMillis() - start, false);
            }

            String output = new String(outputBytes);
            if (output.length() > 50000) output = output.substring(0, 50000);

            return parseJsonResults(suiteName, output, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Failed to run Playwright suite {}: {}", suiteName, e.getMessage());
            return new TestRunResult(suiteName, 0, 0, 0, 0, List.of(), List.of("Execution error: " + e.getMessage()), System.currentTimeMillis() - start, false);
        }
    }

    public List<TestRunResult> runTestSuites(List<String> suiteNames) {
        return suiteNames.stream().map(this::runTestSuite).toList();
    }

    private TestRunResult parseJsonResults(String suiteName, String output, long durationMs) {
        try {
            int jsonStart = output.indexOf('{');
            if (jsonStart < 0) {
                boolean hasPass = output.contains("passed");
                boolean hasFail = output.contains("failed");
                return new TestRunResult(suiteName, 0, 0, 0, 0, List.of(),
                        hasFail ? List.of("Tests failed (non-JSON output)") : List.of(),
                        durationMs, hasPass && !hasFail);
            }

            JsonNode root = objectMapper.readTree(output.substring(jsonStart));
            JsonNode stats = root.path("stats");
            int total = stats.path("expected").asInt() + stats.path("unexpected").asInt() + stats.path("skipped").asInt();
            int passed = stats.path("expected").asInt();
            int failed = stats.path("unexpected").asInt();
            int skipped = stats.path("skipped").asInt();

            List<String> failedTests = new ArrayList<>();
            List<String> errorMessages = new ArrayList<>();

            JsonNode suites = root.path("suites");
            extractFailures(suites, failedTests, errorMessages);

            return new TestRunResult(suiteName, total, passed, failed, skipped, failedTests, errorMessages, durationMs, failed == 0);
        } catch (Exception e) {
            log.warn("Failed to parse Playwright JSON for {}: {}", suiteName, e.getMessage());
            boolean hasPass = output.contains("passed");
            boolean hasFail = output.contains("failed");
            return new TestRunResult(suiteName, 0, 0, 0, 0, List.of(),
                    hasFail ? List.of("Tests failed") : List.of(), durationMs, hasPass && !hasFail);
        }
    }

    private void extractFailures(JsonNode node, List<String> failedTests, List<String> errors) {
        if (node.isArray()) {
            for (JsonNode n : node) extractFailures(n, failedTests, errors);
        } else if (node.isObject()) {
            if (node.has("specs")) {
                for (JsonNode spec : node.path("specs")) {
                    for (JsonNode test : spec.path("tests")) {
                        String status = test.path("status").asText();
                        if ("unexpected".equals(status) || "failed".equals(status)) {
                            failedTests.add(spec.path("title").asText());
                            for (JsonNode result : test.path("results")) {
                                String errMsg = result.path("error").path("message").asText("");
                                if (!errMsg.isEmpty() && errors.size() < 10) {
                                    errors.add(errMsg.length() > 200 ? errMsg.substring(0, 200) : errMsg);
                                }
                            }
                        }
                    }
                }
            }
            if (node.has("suites")) extractFailures(node.path("suites"), failedTests, errors);
        }
    }
}
