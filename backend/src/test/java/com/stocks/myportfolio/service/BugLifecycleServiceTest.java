package com.stocks.myportfolio.service;

import com.stocks.myportfolio.entity.BugReport;
import com.stocks.myportfolio.entity.SupportTicket;
import com.stocks.myportfolio.repository.BugReportRepository;
import com.stocks.myportfolio.repository.SupportTicketRepository;
import com.stocks.myportfolio.repository.TicketActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BugLifecycleServiceTest {

    @Mock private BugReportRepository bugRepo;
    @Mock private SupportTicketRepository ticketRepo;
    @Mock private TicketActivityRepository activityRepo;
    @Mock private AiTicketAgentService aiAgent;
    @Mock private PlaywrightTestRunnerService testRunner;

    private BugLifecycleService service;

    @BeforeEach
    void setup() {
        service = new BugLifecycleService(bugRepo, ticketRepo, activityRepo, aiAgent, testRunner);
    }

    @Test
    void approveBug_setsStatusAndEstimate() {
        BugReport bug = new BugReport();
        bug.setTitle("Test bug");
        bug.setSeverity("HIGH");
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject("Test");
        bug.setTicket(ticket);

        when(bugRepo.findById(1L)).thenReturn(Optional.of(bug));
        when(aiAgent.estimateFixEffort(any())).thenReturn("2-3 hours");
        when(bugRepo.save(any())).thenReturn(bug);
        when(ticketRepo.save(any())).thenReturn(ticket);
        when(activityRepo.save(any())).thenReturn(null);
        when(aiAgent.generateStatusUpdate(any(), any())).thenReturn("Approved");

        BugReport result = service.approveBug(1L, "Looks valid", "HIGH");
        assertEquals("APPROVED", result.getStatus());
        assertNotNull(result.getApprovedAt());
    }

    @Test
    void rejectBug_setsWontFix() {
        BugReport bug = new BugReport();
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject("Test");
        bug.setTicket(ticket);

        when(bugRepo.findById(1L)).thenReturn(Optional.of(bug));
        when(bugRepo.save(any())).thenReturn(bug);
        when(ticketRepo.save(any())).thenReturn(ticket);
        when(activityRepo.save(any())).thenReturn(null);

        BugReport result = service.rejectBug(1L, "Not a bug");
        assertEquals("WONT_FIX", result.getStatus());
    }

    @Test
    void markFixed_closesBugAndTicket() {
        BugReport bug = new BugReport();
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject("Test");
        bug.setTicket(ticket);

        when(bugRepo.findById(1L)).thenReturn(Optional.of(bug));
        when(bugRepo.save(any())).thenReturn(bug);
        when(ticketRepo.save(any())).thenReturn(ticket);
        when(activityRepo.save(any())).thenReturn(null);

        BugReport result = service.markFixed(1L, "Fixed in v2");
        assertEquals("FIXED", result.getStatus());
        assertNotNull(result.getFixedAt());
        assertEquals("CLOSED", ticket.getStatus());
    }

    @Test
    void getBugsByStatus_returnsFiltered() {
        when(bugRepo.findByStatusOrderByCreatedAtDesc("VERIFIED")).thenReturn(List.of(new BugReport()));
        List<BugReport> result = service.getBugsByStatus("VERIFIED");
        assertEquals(1, result.size());
    }

    @Test
    void getBugsByStatus_nullReturnsAll() {
        when(bugRepo.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(new BugReport(), new BugReport()));
        List<BugReport> result = service.getBugsByStatus(null);
        assertEquals(2, result.size());
    }
}
