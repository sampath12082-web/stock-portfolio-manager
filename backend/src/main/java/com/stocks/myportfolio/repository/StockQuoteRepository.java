package com.stocks.myportfolio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.entity.StockQuote;

@Repository
public interface StockQuoteRepository extends JpaRepository<StockQuote, Long> {

    Optional<StockQuote> findTopByStockOrderByFetchedAtDesc(Stock stock);

    Optional<StockQuote> findTopByStockSymbolOrderByFetchedAtDesc(String symbol);
}
