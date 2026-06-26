package com.stocks.myportfolio.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class AiTicketAgentServiceTest {

    @Test
    void classifyLocally_bugKeywords() throws Exception {
        var service = createService();
        assertEquals("BUG_REPORT", invokeClassifyLocally(service, "Login error", "Page crashes when I click login"));
        assertEquals("BUG_REPORT", invokeClassifyLocally(service, "Dashboard broken", "P&L shows wrong values"));
        assertEquals("BUG_REPORT", invokeClassifyLocally(service, "500 error", "Server error on holdings page"));
    }

    @Test
    void classifyLocally_inquiryKeywords() throws Exception {
        var service = createService();
        assertEquals("INQUIRY", invokeClassifyLocally(service, "How to use", "How do I sync my portfolio?"));
        assertEquals("INQUIRY", invokeClassifyLocally(service, "Question", "What is the meaning of LTP?"));
    }

    @Test
    void classifyLocally_featureKeywords() throws Exception {
        var service = createService();
        assertEquals("FEATURE_REQUEST", invokeClassifyLocally(service, "Please add dark mode", "Would love a dark theme"));
        assertEquals("FEATURE_REQUEST", invokeClassifyLocally(service, "Feature suggestion", "Can you implement watchlist?"));
    }

    @Test
    void classifyLocally_feedbackKeywords() throws Exception {
        var service = createService();
        assertEquals("FEEDBACK", invokeClassifyLocally(service, "Great app", "Love the dashboard, great job!"));
        assertEquals("FEEDBACK", invokeClassifyLocally(service, "Thank you", "Appreciate the quick updates"));
    }

    @Test
    void mapBugToTestSuites_authKeywords() {
        var service = createService();
        List<String> suites = service.mapBugToTestSuites("Login fails", "Cannot login with correct password");
        assertTrue(suites.contains("auth"));
    }

    @Test
    void mapBugToTestSuites_dashboardKeywords() {
        var service = createService();
        List<String> suites = service.mapBugToTestSuites("Dashboard P&L wrong", "Invested amount shows 0");
        assertTrue(suites.contains("smoke"));
        assertTrue(suites.contains("regression"));
    }

    @Test
    void mapBugToTestSuites_uiKeywords() {
        var service = createService();
        List<String> suites = service.mapBugToTestSuites("Blank page", "Holdings page not showing any data");
        assertTrue(suites.contains("ui-rendering"));
    }

    @Test
    void mapBugToTestSuites_defaultsToSmoke() {
        var service = createService();
        List<String> suites = service.mapBugToTestSuites("Something weird", "I don't know what happened");
        assertTrue(suites.contains("smoke"));
    }

    private AiTicketAgentService createService() {
        return new AiTicketAgentService(null, null, null, null, null);
    }

    private String invokeClassifyLocally(AiTicketAgentService service, String subject, String message) throws Exception {
        var method = AiTicketAgentService.class.getDeclaredMethod("classifyLocally", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, subject, message);
    }
}
