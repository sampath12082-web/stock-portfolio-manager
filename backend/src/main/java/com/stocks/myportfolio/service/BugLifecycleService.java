package com.stocks.myportfolio.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.stocks.myportfolio.dto.response.TestRunResult;
import com.stocks.myportfolio.entity.BugReport;
import com.stocks.myportfolio.entity.SupportTicket;
import com.stocks.myportfolio.entity.TicketActivity;
import com.stocks.myportfolio.repository.BugReportRepository;
import com.stocks.myportfolio.repository.SupportTicketRepository;
import com.stocks.myportfolio.repository.TicketActivityRepository;

@Service
public class BugLifecycleService {

    private final BugReportRepository bugRepo;
    private final SupportTicketRepository ticketRepo;
    private final TicketActivityRepository activityRepo;
    private final AiTicketAgentService aiAgent;
    private final PlaywrightTestRunnerService testRunner;

    public BugLifecycleService(BugReportRepository bugRepo, SupportTicketRepository ticketRepo,
            TicketActivityRepository activityRepo, AiTicketAgentService aiAgent, PlaywrightTestRunnerService testRunner) {
        this.bugRepo = bugRepo;
        this.ticketRepo = ticketRepo;
        this.activityRepo = activityRepo;
        this.aiAgent = aiAgent;
        this.testRunner = testRunner;
    }

    public BugReport approveBug(Long bugId, String adminNotes, String priority) {
        BugReport bug = bugRepo.findById(bugId).orElseThrow();
        SupportTicket ticket = bug.getTicket();

        bug.setStatus("APPROVED");
        bug.setAdminNotes(adminNotes);
        bug.setApprovedAt(LocalDateTime.now());
        bug.setUpdatedAt(LocalDateTime.now());
        if (priority != null) bug.setSeverity(priority);

        String estimate = aiAgent.estimateFixEffort(bug);
        bug.setEstimatedFixDescription(estimate);

        ticket.setStatus("APPROVED");
        ticket.setPriority(priority != null ? priority : ticket.getPriority());
        String response = aiAgent.generateStatusUpdate(ticket, "APPROVED");
        ticket.setAiResponse(response + "\n\nEstimated effort: " + estimate);
        ticket.setUpdatedAt(LocalDateTime.now());

        bugRepo.save(bug);
        ticketRepo.save(ticket);
        logActivity(ticket, "ADMIN", "BUG_APPROVED", "Priority: " + priority + ". " + (adminNotes != null ? adminNotes : ""));
        return bug;
    }

    public BugReport rejectBug(Long bugId, String adminNotes) {
        BugReport bug = bugRepo.findById(bugId).orElseThrow();
        SupportTicket ticket = bug.getTicket();

        bug.setStatus("WONT_FIX");
        bug.setAdminNotes(adminNotes);
        bug.setUpdatedAt(LocalDateTime.now());

        ticket.setStatus("RESOLVED");
        ticket.setAiResponse("After review, this issue has been determined to be " +
                (adminNotes != null ? "not actionable: " + adminNotes : "not a bug. No changes needed."));
        ticket.setUpdatedAt(LocalDateTime.now());

        bugRepo.save(bug);
        ticketRepo.save(ticket);
        logActivity(ticket, "ADMIN", "BUG_REJECTED", adminNotes != null ? adminNotes : "");
        return bug;
    }

    public BugReport startDevelopment(Long bugId) {
        BugReport bug = bugRepo.findById(bugId).orElseThrow();
        SupportTicket ticket = bug.getTicket();

        bug.setStatus("IN_DEVELOPMENT");
        bug.setUpdatedAt(LocalDateTime.now());
        ticket.setStatus("IN_DEVELOPMENT");
        ticket.setAiResponse(aiAgent.generateStatusUpdate(ticket, "IN_DEVELOPMENT"));
        ticket.setUpdatedAt(LocalDateTime.now());

        bugRepo.save(bug);
        ticketRepo.save(ticket);
        logActivity(ticket, "ADMIN", "DEV_STARTED", "Development started");
        return bug;
    }

    public BugReport markFixed(Long bugId, String resolution) {
        BugReport bug = bugRepo.findById(bugId).orElseThrow();
        SupportTicket ticket = bug.getTicket();

        bug.setStatus("FIXED");
        bug.setFixedAt(LocalDateTime.now());
        bug.setUpdatedAt(LocalDateTime.now());
        if (resolution != null) bug.setAdminNotes((bug.getAdminNotes() != null ? bug.getAdminNotes() + "\n" : "") + "Resolution: " + resolution);

        ticket.setStatus("CLOSED");
        ticket.setAiResponse("Your reported issue has been fixed! Resolution: " + (resolution != null ? resolution : "Bug addressed in latest update.") +
                " Thank you for helping us improve the platform.");
        ticket.setUpdatedAt(LocalDateTime.now());

        bugRepo.save(bug);
        ticketRepo.save(ticket);
        logActivity(ticket, "SYSTEM", "BUG_FIXED", resolution != null ? resolution : "Bug fixed");
        return bug;
    }

    public BugReport rerunTests(Long bugId) {
        BugReport bug = bugRepo.findById(bugId).orElseThrow();
        SupportTicket ticket = bug.getTicket();

        List<String> suites = bug.getTestSuite() != null ? List.of(bug.getTestSuite().split(",")) : List.of("smoke");
        List<TestRunResult> results = testRunner.runTestSuites(suites);

        int totalFailed = results.stream().mapToInt(TestRunResult::failed).sum();
        String summary = results.stream()
                .map(r -> r.suiteName() + ": " + r.passed() + "P/" + r.failed() + "F")
                .reduce((a, b) -> a + ", " + b).orElse("No results");

        bug.setTestResult(summary);
        bug.setTestPassed(totalFailed == 0);
        bug.setTestRunAt(LocalDateTime.now());
        bug.setUpdatedAt(LocalDateTime.now());
        bugRepo.save(bug);

        logActivity(ticket, "SYSTEM", "TESTS_RERUN", summary);
        return bug;
    }

    public List<BugReport> getBugsByStatus(String status) {
        if (status == null || status.isBlank()) return bugRepo.findAllByOrderByCreatedAtDesc();
        return bugRepo.findByStatusOrderByCreatedAtDesc(status);
    }

    private void logActivity(SupportTicket ticket, String actor, String action, String detail) {
        activityRepo.save(new TicketActivity(ticket, actor, action, detail));
    }
}
