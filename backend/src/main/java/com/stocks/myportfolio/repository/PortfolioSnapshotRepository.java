package com.stocks.myportfolio.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocks.myportfolio.entity.PortfolioSnapshot;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {

    Optional<PortfolioSnapshot> findBySnapshotDate(LocalDate date);

    List<PortfolioSnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(
            LocalDate from, LocalDate to);

    Optional<PortfolioSnapshot> findTopByOrderBySnapshotDateDesc();

    List<PortfolioSnapshot> findTop30ByOrderBySnapshotDateDesc();
    List<PortfolioSnapshot> findByUserId(Long userId);
}
