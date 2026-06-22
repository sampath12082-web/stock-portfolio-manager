package com.stocks.myportfolio.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.entity.*;
import com.stocks.myportfolio.repository.*;
import com.stocks.myportfolio.service.AiTicketAgentService;

@RestController
public class HelpController {

    private final FaqRepository faqRepository;
    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketActivityRepository activityRepository;
    private final AiTicketAgentService aiAgent;

    public HelpController(FaqRepository faqRepository, SupportTicketRepository ticketRepository,
            UserRepository userRepository, TicketActivityRepository activityRepository,
            AiTicketAgentService aiAgent) {
        this.faqRepository = faqRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.aiAgent = aiAgent;
    }

    @GetMapping("/api/help/faq")
    public List<Faq> getFaqs() {
        return faqRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    @PostMapping("/api/help/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createTicket(Principal principal, @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setSubject(body.get("subject"));
        ticket.setMessage(body.get("message"));
        ticket.setStatus("OPEN");

        SupportTicket saved = ticketRepository.save(ticket);
        aiAgent.processNewTicket(saved.getId());

        return Map.of("id", saved.getId(), "message", "Ticket submitted — AI agent is reviewing your request");
    }

    @GetMapping("/api/help/tickets")
    public List<Map<String, Object>> getMyTickets(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ticketRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toTicketMap).toList();
    }

    @GetMapping("/api/help/tickets/{id}/activity")
    public List<Map<String, Object>> getTicketActivity(@PathVariable Long id) {
        return activityRepository.findByTicketIdOrderByCreatedAtAsc(id).stream()
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("actor", a.getActor());
                    m.put("action", a.getAction());
                    m.put("detail", a.getDetail());
                    m.put("createdAt", a.getCreatedAt().toString());
                    return m;
                }).toList();
    }

    @GetMapping("/api/admin/tickets")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Map<String, Object>> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toTicketMap).toList();
    }

    @PutMapping("/api/admin/tickets/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Map<String, Object> respondToTicket(@PathVariable Long id, @RequestBody Map<String, String> body) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        if (body.containsKey("adminResponse")) {
            ticket.setAdminResponse(body.get("adminResponse"));
            ticket.setRespondedAt(LocalDateTime.now());
        }
        if (body.containsKey("status")) {
            ticket.setStatus(body.get("status"));
        }
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        activityRepository.save(new TicketActivity(ticket, "ADMIN", "RESPONDED", body.getOrDefault("adminResponse", "")));
        return toTicketMap(ticket);
    }

    @PostMapping("/api/admin/faq")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Faq createFaq(@RequestBody Map<String, String> body) {
        Faq faq = new Faq();
        faq.setCategory(body.get("category"));
        faq.setQuestion(body.get("question"));
        faq.setAnswer(body.get("answer"));
        faq.setSortOrder(Integer.parseInt(body.getOrDefault("sortOrder", "0")));
        return faqRepository.save(faq);
    }

    @PutMapping("/api/admin/faq/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Faq updateFaq(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found"));
        if (body.containsKey("category")) faq.setCategory(body.get("category"));
        if (body.containsKey("question")) faq.setQuestion(body.get("question"));
        if (body.containsKey("answer")) faq.setAnswer(body.get("answer"));
        if (body.containsKey("active")) faq.setActive(Boolean.parseBoolean(body.get("active")));
        if (body.containsKey("sortOrder")) faq.setSortOrder(Integer.parseInt(body.get("sortOrder")));
        return faqRepository.save(faq);
    }

    @DeleteMapping("/api/admin/faq/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Map<String, String> deleteFaq(@PathVariable Long id) {
        faqRepository.deleteById(id);
        return Map.of("message", "FAQ deleted");
    }

    private Map<String, Object> toTicketMap(SupportTicket t) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", t.getId());
        map.put("userEmail", t.getUser().getEmail());
        map.put("userName", t.getUser().getFirstName() + " " + (t.getUser().getLastName() != null ? t.getUser().getLastName() : ""));
        map.put("subject", t.getSubject());
        map.put("message", t.getMessage());
        map.put("status", t.getStatus());
        map.put("ticketType", t.getTicketType() != null ? t.getTicketType() : "INQUIRY");
        map.put("priority", t.getPriority() != null ? t.getPriority() : "MEDIUM");
        map.put("adminResponse", t.getAdminResponse() != null ? t.getAdminResponse() : "");
        map.put("aiResponse", t.getAiResponse() != null ? t.getAiResponse() : "");
        map.put("createdAt", t.getCreatedAt().toString());
        if (t.getBugReport() != null) {
            BugReport b = t.getBugReport();
            Map<String, Object> bug = new LinkedHashMap<>();
            bug.put("id", b.getId());
            bug.put("title", b.getTitle());
            bug.put("severity", b.getSeverity());
            bug.put("status", b.getStatus());
            bug.put("testPassed", b.getTestPassed());
            bug.put("testResult", b.getTestResult());
            bug.put("estimatedFixHours", b.getEstimatedFixHours());
            bug.put("estimatedFixDescription", b.getEstimatedFixDescription());
            map.put("bugReport", bug);
        }
        return map;
    }
}
