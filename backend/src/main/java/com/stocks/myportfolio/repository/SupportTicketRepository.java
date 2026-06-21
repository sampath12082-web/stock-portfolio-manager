package com.stocks.myportfolio.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.stocks.myportfolio.entity.SupportTicket;
import com.stocks.myportfolio.entity.User;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByUserOrderByCreatedAtDesc(User user);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}
