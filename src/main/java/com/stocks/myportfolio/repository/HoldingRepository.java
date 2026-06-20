package com.stocks.myportfolio.repository;

import com.stocks.myportfolio.entity.Holding;
import com.stocks.myportfolio.entity.Stock;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository
        extends JpaRepository<Holding, Long> {

                Optional<Holding> findByStock(Stock stock);
}