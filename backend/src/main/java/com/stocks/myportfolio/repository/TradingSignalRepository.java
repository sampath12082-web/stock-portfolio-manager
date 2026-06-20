package com.stocks.myportfolio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocks.myportfolio.common.enums.SignalStatus;
import com.stocks.myportfolio.common.enums.SignalType;
import com.stocks.myportfolio.entity.TradingSignal;

@Repository
public interface TradingSignalRepository extends JpaRepository<TradingSignal, Long> {

    List<TradingSignal> findBySignalDateOrderByCreatedAtDesc(LocalDate date);

    List<TradingSignal> findByStatusOrderBySignalDateDesc(SignalStatus status);

    List<TradingSignal> findBySignalTypeAndStatusOrderBySignalDateDesc(
            SignalType type, SignalStatus status);

    List<TradingSignal> findBySignalDateBetweenOrderBySignalDateDesc(
            LocalDate from, LocalDate to);

    List<TradingSignal> findBySymbolIgnoreCaseOrderBySignalDateDesc(String symbol);

    List<TradingSignal> findByStatusAndSignalDateBefore(
            SignalStatus status, LocalDate date);
}
