package com.stocks.myportfolio.repository;

import com.stocks.myportfolio.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository
        extends JpaRepository<Holding, Long> {
}