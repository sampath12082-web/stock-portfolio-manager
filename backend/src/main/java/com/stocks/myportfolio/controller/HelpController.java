package com.stocks.myportfolio.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.entity.Faq;
import com.stocks.myportfolio.entity.SupportTicket;
import com.stocks.myportfolio.entity.User;
import com.stocks.myportfolio.repository.FaqRepository;
import com.stocks.myportfolio.repository.SupportTicketRepository;
import com.stocks.myportfolio.repository.UserRepository;

@RestController
public class HelpController {

    private final FaqRepository faqRepository;
    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;

    public HelpController(FaqRepository faqRepository, SupportTicketRepository ticketRepository,
            UserRepository userRepository) {
        this.faqRepository = faqRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
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
        return Map.of("id", saved.getId(), "message", "Ticket submitted successfully");
    }

    @GetMapping("/api/help/tickets")
    public List<Map<String, Object>> getMyTickets(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ticketRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toTicketMap).toList();
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
        ticketRepository.save(ticket);
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
        return Map.of(
                "id", t.getId(),
                "userEmail", t.getUser().getEmail(),
                "userName", t.getUser().getFirstName() + " " + (t.getUser().getLastName() != null ? t.getUser().getLastName() : ""),
                "subject", t.getSubject(),
                "message", t.getMessage(),
                "status", t.getStatus(),
                "adminResponse", t.getAdminResponse() != null ? t.getAdminResponse() : "",
                "createdAt", t.getCreatedAt().toString());
    }
}
