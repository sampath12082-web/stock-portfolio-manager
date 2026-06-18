package com.stocks.myportfolio.repository;

import com.stocks.myportfolio.entity.Stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);

    Optional<Stock> findBySymbolIgnoreCase(String symbol);
}