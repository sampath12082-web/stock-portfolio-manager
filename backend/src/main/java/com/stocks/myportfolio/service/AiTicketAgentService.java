package com.stocks.myportfolio.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocks.myportfolio.dto.response.TestRunResult;
import com.stocks.myportfolio.entity.BugReport;
import com.stocks.myportfolio.entity.SupportTicket;
import com.stocks.myportfolio.entity.TicketActivity;
import com.stocks.myportfolio.repository.BugReportRepository;
import com.stocks.myportfolio.repository.SupportTicketRepository;
import com.stocks.myportfolio.repository.TicketActivityRepository;

@Service
public class AiTicketAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiTicketAgentService.class);

    @Value("${anthropic.api-key:}")
    private String anthropicApiKey;

    private final SupportTicketRepository ticketRepo;
    private final BugReportRepository bugRepo;
    private final TicketActivityRepository activityRepo;
    private final PlaywrightTestRunnerService testRunner;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiTicketAgentService(SupportTicketRepository ticketRepo, BugReportRepository bugRepo,
            TicketActivityRepository activityRepo, PlaywrightTestRunnerService testRunner) {
        this.ticketRepo = ticketRepo;
        this.bugRepo = bugRepo;
        this.activityRepo = activityRepo;
        this.testRunner = testRunner;
    }

    @Async("ticketAgentExecutor")
    public void processNewTicket(Long ticketId) {
        try {
            SupportTicket ticket = ticketRepo.findById(ticketId).orElse(null);
            if (ticket == null) return;

            String type = classifyTicket(ticket.getSubject(), ticket.getMessage());
            String priority = "MEDIUM";
            ticket.setTicketType(type);

            logActivity(ticket, "AI_AGENT", "CLASSIFIED", "Type: " + type);

            switch (type) {
                case "BUG_REPORT" -> handleBugReport(ticket);
                case "INQUIRY" -> handleInquiry(ticket);
                case "FEATURE_REQUEST" -> handleFeatureRequest(ticket);
                case "FEEDBACK" -> handleFeedback(ticket);
            }

            ticket.setAiReviewedAt(LocalDateTime.now());
            ticket.setUpdatedAt(LocalDateTime.now());
            ticketRepo.save(ticket);
        } catch (Exception e) {
            log.error("AI agent failed for ticket {}: {}", ticketId, e.getMessage());
        }
    }

    private String classifyTicket(String subject, String message) {
        if (anthropicApiKey != null && !anthropicApiKey.isBlank()) {
            try {
                String prompt = String.format(
                        "Classify this support ticket for a stock portfolio management app.\n\n" +
                        "Subject: %s\nMessage: %s\n\n" +
                        "Respond with ONLY one of: BUG_REPORT, INQUIRY, FEATURE_REQUEST, FEEDBACK",
                        subject, message);
                String result = callClaude("You are a support ticket classifier. Respond with exactly one classification word.", prompt);
                String cleaned = result.trim().toUpperCase().replaceAll("[^A-Z_]", "");
                if (Set.of("BUG_REPORT", "INQUIRY", "FEATURE_REQUEST", "FEEDBACK").contains(cleaned)) {
                    return cleaned;
                }
            } catch (Exception e) {
                log.warn("Claude classification failed, using local: {}", e.getMessage());
            }
        }
        return classifyLocally(subject, message);
    }

    private String classifyLocally(String subject, String message) {
        String combined = (subject + " " + message).toLowerCase();
        if (combined.matches(".*\\b(bug|error|crash|broken|not working|fail|500|404|wrong|incorrect|missing|blank|empty page)\\b.*"))
            return "BUG_REPORT";
        if (combined.matches(".*\\b(feature|add|request|want|wish|could you|please add|suggestion|implement)\\b.*"))
            return "FEATURE_REQUEST";
        if (combined.matches(".*\\b(thank|great|love|feedback|appreciate|nice|good job|well done)\\b.*"))
            return "FEEDBACK";
        return "INQUIRY";
    }

    private void handleBugReport(SupportTicket ticket) {
        BugReport bug = new BugReport();
        bug.setTicket(ticket);
        bug.setTitle(ticket.getSubject());
        bug.setDescription(ticket.getMessage());
        bug.setSeverity("MEDIUM");
        bug.setStatus("PENDING_VERIFICATION");

        List<String> suites = mapBugToTestSuites(ticket.getSubject(), ticket.getMessage());
        bug.setTestSuite(String.join(",", suites));
        bugRepo.save(bug);
        ticket.setBugReport(bug);
        ticket.setStatus("AI_REVIEWED");
        ticket.setAiResponse("Your bug report has been received. Running automated tests to verify the issue...");
        ticketRepo.save(ticket);

        logActivity(ticket, "AI_AGENT", "BUG_CREATED", "Bug #" + bug.getId() + " — running tests: " + bug.getTestSuite());

        try {
            List<TestRunResult> results = testRunner.runTestSuites(suites);
            handleTestResults(ticket, bug, results);
        } catch (Exception e) {
            log.error("Test execution failed for bug {}: {}", bug.getId(), e.getMessage());
            bug.setStatus("PENDING_VERIFICATION");
            bug.setTestResult("Test execution failed: " + e.getMessage());
            bugRepo.save(bug);
            ticket.setAiResponse("Your bug report has been received. Automated testing is temporarily unavailable — our team will review manually.");
            ticketRepo.save(ticket);
        }
    }

    private void handleTestResults(SupportTicket ticket, BugReport bug, List<TestRunResult> results) {
        int totalFailed = results.stream().mapToInt(TestRunResult::failed).sum();
        int totalPassed = results.stream().mapToInt(TestRunResult::passed).sum();
        List<String> allFailed = results.stream().flatMap(r -> r.failedTests().stream()).toList();

        String summary = results.stream()
                .map(r -> r.suiteName() + ": " + r.passed() + " passed, " + r.failed() + " failed")
                .collect(Collectors.joining("; "));

        bug.setTestResult(summary);
        bug.setTestPassed(totalFailed == 0);
        bug.setTestRunAt(LocalDateTime.now());

        if (totalFailed > 0) {
            bug.setStatus("VERIFIED");
            ticket.setStatus("BUG_CONFIRMED");
            String failedList = allFailed.stream().limit(5).collect(Collectors.joining(", "));
            ticket.setAiResponse(String.format(
                    "Bug verified! %d test(s) failed: %s. Results: %s. This has been escalated for admin review.",
                    totalFailed, failedList, summary));
            logActivity(ticket, "AI_AGENT", "BUG_VERIFIED", totalFailed + " tests failed: " + failedList);
        } else {
            bug.setStatus("NOT_REPRODUCIBLE");
            ticket.setStatus("BUG_NOT_CONFIRMED");
            ticket.setAiResponse(String.format(
                    "All %d automated tests passed (%s). The reported issue could not be reproduced automatically. " +
                    "Our team will review your report manually for issues not covered by tests.",
                    totalPassed, summary));
            logActivity(ticket, "AI_AGENT", "BUG_NOT_REPRODUCED", "All " + totalPassed + " tests passed");
        }

        bug.setUpdatedAt(LocalDateTime.now());
        bugRepo.save(bug);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepo.save(ticket);
    }

    private void handleInquiry(SupportTicket ticket) {
        String response = generateInquiryResponse(ticket.getSubject(), ticket.getMessage());
        ticket.setAiResponse(response);
        ticket.setStatus("AI_REVIEWED");
        logActivity(ticket, "AI_AGENT", "RESPONDED", "Inquiry auto-responded");
    }

    private void handleFeatureRequest(SupportTicket ticket) {
        ticket.setAiResponse("Thank you for your feature suggestion! Our team will review your request and consider it for future development. " +
                "You'll be notified when there's an update on this request.");
        ticket.setStatus("AI_REVIEWED");
        logActivity(ticket, "AI_AGENT", "RESPONDED", "Feature request acknowledged");
    }

    private void handleFeedback(SupportTicket ticket) {
        ticket.setAiResponse("Thank you for your feedback! We truly appreciate you taking the time to share your thoughts. " +
                "Your input helps us improve the platform.");
        ticket.setStatus("AI_REVIEWED");
        logActivity(ticket, "AI_AGENT", "RESPONDED", "Feedback acknowledged");
    }

    private String generateInquiryResponse(String subject, String message) {
        if (anthropicApiKey != null && !anthropicApiKey.isBlank()) {
            try {
                String systemPrompt = "You are a helpful support agent for SoloSprint Trade, an Indian stock portfolio management app " +
                        "(NSE/BSE). Features include: Dashboard (P&L, Groww account, mutual funds), Holdings, Transactions, Stocks, " +
                        "Mutual Funds (AMFI NAV), Performance tracking, Trading Signals (SMA/RSI), Groww broker sync, and AI Stock Search. " +
                        "Respond concisely in 2-4 sentences. Be specific about which page or feature to use.";
                return callClaude(systemPrompt, "Subject: " + subject + "\nQuestion: " + message);
            } catch (Exception e) {
                log.warn("Claude inquiry response failed: {}", e.getMessage());
            }
        }
        return generateLocalInquiryResponse(subject, message);
    }

    private String generateLocalInquiryResponse(String subject, String message) {
        String combined = (subject + " " + message).toLowerCase();
        if (combined.contains("portfolio") || combined.contains("dashboard"))
            return "Your portfolio overview is on the Dashboard page — it shows Total Funds, Portfolio P&L, Mutual Funds summary, and Groww account data. Click any section for detailed breakdowns.";
        if (combined.contains("groww") || combined.contains("sync"))
            return "Groww sync can be configured from your Profile page (Groww Config section). Once enabled, use the 'Sync from Groww' button on the Holdings page to import your positions.";
        if (combined.contains("signal") || combined.contains("buy") || combined.contains("sell"))
            return "Trading signals are generated automatically using SMA crossover, RSI, and 52-week analysis. View them on the Stocks page (Signal filter chips) or Dashboard (Trading Signals section).";
        if (combined.contains("mutual fund") || combined.contains("nav") || combined.contains("mf"))
            return "Mutual fund data is on the Mutual Funds page. NAVs refresh nightly at 9 PM from AMFI. You can manually refresh via the 'Refresh NAV' button.";
        if (combined.contains("password") || combined.contains("login") || combined.contains("account"))
            return "For password issues, use 'Forgot Password' on the login page (3-step: email → security questions → OTP). Passwords must be 16-20 chars with upper, lower, digit, and special character.";
        if (combined.contains("performance") || combined.contains("snapshot") || combined.contains("chart"))
            return "The Performance page shows portfolio value over time. Snapshots are captured daily at 3:30 PM. Use 'Capture Snapshot' to create one manually.";
        return "Thank you for your inquiry. Our team will review this and respond shortly. In the meantime, check our FAQ section for common answers.";
    }

    public String estimateFixEffort(BugReport bug) {
        if (anthropicApiKey != null && !anthropicApiKey.isBlank()) {
            try {
                String prompt = String.format(
                        "Bug: %s\nDescription: %s\nTest results: %s\nSeverity: %s\n\n" +
                        "Estimate the fix effort for this bug in a stock portfolio web app (Spring Boot + React). " +
                        "Respond with: estimated hours (integer), brief description of what needs to change.",
                        bug.getTitle(), bug.getDescription(), bug.getTestResult(), bug.getSeverity());
                return callClaude("You are a software estimation expert. Be concise.", prompt);
            } catch (Exception e) {
                log.warn("Claude estimation failed: {}", e.getMessage());
            }
        }
        return switch (bug.getSeverity()) {
            case "CRITICAL" -> "Estimated 4-8 hours. Critical bugs typically require immediate investigation and hotfix.";
            case "HIGH" -> "Estimated 2-4 hours. Likely requires changes to backend service logic or frontend rendering.";
            case "LOW" -> "Estimated 1 hour. Minor fix, likely a UI or configuration adjustment.";
            default -> "Estimated 2-3 hours. Standard bug fix involving investigation, code change, and testing.";
        };
    }

    public String generateStatusUpdate(SupportTicket ticket, String statusChange) {
        if (anthropicApiKey != null && !anthropicApiKey.isBlank()) {
            try {
                String prompt = String.format("Ticket: %s\nStatus changed to: %s\nGenerate a brief user-friendly status update (1-2 sentences).",
                        ticket.getSubject(), statusChange);
                return callClaude("You are a support agent. Write a concise status update for the user.", prompt);
            } catch (Exception e) {
                log.warn("Claude status update failed: {}", e.getMessage());
            }
        }
        return switch (statusChange) {
            case "APPROVED" -> "Your reported issue has been approved for development. We'll provide a fix estimate shortly.";
            case "IN_DEVELOPMENT" -> "Development has started on your reported issue. We'll notify you once the fix is ready.";
            case "RESOLVED" -> "Your issue has been resolved! The fix will be available in the next deployment.";
            case "CLOSED" -> "This ticket has been closed. If you experience further issues, please submit a new ticket.";
            default -> "Your ticket status has been updated to: " + statusChange;
        };
    }

    List<String> mapBugToTestSuites(String subject, String message) {
        String combined = (subject + " " + message).toLowerCase();
        List<String> suites = new ArrayList<>();

        if (combined.matches(".*\\b(login|auth|password|register|otp|security|logout)\\b.*"))
            suites.add("auth");
        if (combined.matches(".*\\b(dashboard|p&l|balance|invested|current value|net worth|sector|chart)\\b.*")) {
            suites.add("smoke");
            suites.add("regression");
        }
        if (combined.matches(".*\\b(holding|stock|transaction|buy|sell|signal|mutual fund|mf|nav|faq|help|ticket)\\b.*"))
            suites.add("functional");
        if (combined.matches(".*\\b(ui|display|render|page|layout|button|blank|broken page|not showing)\\b.*"))
            suites.add("ui-rendering");

        if (suites.isEmpty()) suites.add("smoke");
        return suites.stream().distinct().toList();
    }

    private String callClaude(String systemPrompt, String userMessage) throws Exception {
        RestClient client = RestClient.create();
        String body = objectMapper.writeValueAsString(Map.of(
                "model", "claude-sonnet-4-6",
                "max_tokens", 500,
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userMessage))
        ));

        String response = client.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", anthropicApiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .body(body)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);
        return root.path("content").get(0).path("text").asText();
    }

    private void logActivity(SupportTicket ticket, String actor, String action, String detail) {
        activityRepo.save(new TicketActivity(ticket, actor, action, detail));
    }
}
