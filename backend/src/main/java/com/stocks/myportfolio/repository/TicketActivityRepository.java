package com.stocks.myportfolio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocks.myportfolio.entity.TicketActivity;

@Repository
public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long> {

    List<TicketActivity> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
