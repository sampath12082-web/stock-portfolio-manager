package com.stocks.myportfolio.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocks.myportfolio.common.enums.TransactionType;
import com.stocks.myportfolio.entity.Stock;
import com.stocks.myportfolio.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByOrderByCreatedAtDesc();

    List<Transaction> findByStockOrderByCreatedAtDesc(Stock stock);

    List<Transaction> findByTransactionTypeOrderByCreatedAtDesc(TransactionType type);

    List<Transaction> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end);
}
