package com.stocks.myportfolio.controller;

import java.util.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.entity.BugReport;
import com.stocks.myportfolio.service.BugLifecycleService;

@RestController
@RequestMapping("/api/admin/bugs")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class BugReportController {

    private final BugLifecycleService bugService;

    public BugReportController(BugLifecycleService bugService) {
        this.bugService = bugService;
    }

    @GetMapping
    public List<Map<String, Object>> listBugs(@RequestParam(required = false) String status) {
        return bugService.getBugsByStatus(status).stream().map(this::toBugMap).toList();
    }

    @PutMapping("/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return toBugMap(bugService.approveBug(id, body.get("adminNotes"), body.get("priority")));
    }

    @PutMapping("/{id}/reject")
    public Map<String, Object> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return toBugMap(bugService.rejectBug(id, body.get("adminNotes")));
    }

    @PutMapping("/{id}/start-development")
    public Map<String, Object> startDev(@PathVariable Long id) {
        return toBugMap(bugService.startDevelopment(id));
    }

    @PutMapping("/{id}/mark-fixed")
    public Map<String, Object> markFixed(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return toBugMap(bugService.markFixed(id, body.get("resolution")));
    }

    @PostMapping("/{id}/rerun-tests")
    public Map<String, Object> rerunTests(@PathVariable Long id) {
        return toBugMap(bugService.rerunTests(id));
    }

    private Map<String, Object> toBugMap(BugReport b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("ticketId", b.getTicket().getId());
        m.put("title", b.getTitle());
        m.put("description", b.getDescription());
        m.put("severity", b.getSeverity());
        m.put("status", b.getStatus());
        m.put("testSuite", b.getTestSuite());
        m.put("testResult", b.getTestResult());
        m.put("testPassed", b.getTestPassed());
        m.put("testRunAt", b.getTestRunAt() != null ? b.getTestRunAt().toString() : null);
        m.put("adminNotes", b.getAdminNotes());
        m.put("approvedAt", b.getApprovedAt() != null ? b.getApprovedAt().toString() : null);
        m.put("estimatedFixHours", b.getEstimatedFixHours());
        m.put("estimatedFixDescription", b.getEstimatedFixDescription());
        m.put("fixedAt", b.getFixedAt() != null ? b.getFixedAt().toString() : null);
        m.put("createdAt", b.getCreatedAt().toString());
        return m;
    }
}
