package com.stocks.myportfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bug_report")
public class BugReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String severity = "MEDIUM";

    @Column(nullable = false)
    private String status = "PENDING_VERIFICATION";

    @Column(name = "test_suite")
    private String testSuite;

    @Column(name = "test_result", columnDefinition = "TEXT")
    private String testResult;

    @Column(name = "test_passed")
    private Boolean testPassed;

    @Column(name = "test_run_at")
    private LocalDateTime testRunAt;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "estimated_fix_hours")
    private Integer estimatedFixHours;

    @Column(name = "estimated_fix_description", columnDefinition = "TEXT")
    private String estimatedFixDescription;

    @Column(name = "fixed_at")
    private LocalDateTime fixedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BugReport() {}

    public Long getId() { return id; }
    public SupportTicket getTicket() { return ticket; }
    public void setTicket(SupportTicket ticket) { this.ticket = ticket; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTestSuite() { return testSuite; }
    public void setTestSuite(String testSuite) { this.testSuite = testSuite; }
    public String getTestResult() { return testResult; }
    public void setTestResult(String testResult) { this.testResult = testResult; }
    public Boolean getTestPassed() { return testPassed; }
    public void setTestPassed(Boolean testPassed) { this.testPassed = testPassed; }
    public LocalDateTime getTestRunAt() { return testRunAt; }
    public void setTestRunAt(LocalDateTime testRunAt) { this.testRunAt = testRunAt; }
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public Integer getEstimatedFixHours() { return estimatedFixHours; }
    public void setEstimatedFixHours(Integer estimatedFixHours) { this.estimatedFixHours = estimatedFixHours; }
    public String getEstimatedFixDescription() { return estimatedFixDescription; }
    public void setEstimatedFixDescription(String desc) { this.estimatedFixDescription = desc; }
    public LocalDateTime getFixedAt() { return fixedAt; }
    public void setFixedAt(LocalDateTime fixedAt) { this.fixedAt = fixedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
