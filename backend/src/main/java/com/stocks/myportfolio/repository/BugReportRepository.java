package com.stocks.myportfolio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocks.myportfolio.entity.BugReport;

@Repository
public interface BugReportRepository extends JpaRepository<BugReport, Long> {

    List<BugReport> findByStatusOrderByCreatedAtDesc(String status);

    List<BugReport> findAllByOrderByCreatedAtDesc();

    Optional<BugReport> findByTicketId(Long ticketId);
}
