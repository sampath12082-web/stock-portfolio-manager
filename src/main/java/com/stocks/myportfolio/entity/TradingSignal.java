package com.stocks.myportfolio.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.stocks.myportfolio.common.enums.SignalSource;
import com.stocks.myportfolio.common.enums.SignalStatus;
import com.stocks.myportfolio.common.enums.SignalType;

import jakarta.persistence.*;

@Entity
@Table(name = "trading_signal")
public class TradingSignal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false, length = 20)
    private SignalType signalType;

    @Column(name = "target_price")
    private BigDecimal targetPrice;

    @Column(name = "stop_loss")
    private BigDecimal stopLoss;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "signal_date", nullable = false)
    private LocalDate signalDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SignalStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SignalSource source = SignalSource.MANUAL;

    public TradingSignal() {
    }

    public Long getId() {
        return id;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public SignalType getSignalType() {
        return signalType;
    }

    public void setSignalType(SignalType signalType) {
        this.signalType = signalType;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }

    public BigDecimal getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(BigDecimal stopLoss) {
        this.stopLoss = stopLoss;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public LocalDate getSignalDate() {
        return signalDate;
    }

    public void setSignalDate(LocalDate signalDate) {
        this.signalDate = signalDate;
    }

    public SignalStatus getStatus() {
        return status;
    }

    public void setStatus(SignalStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public SignalSource getSource() {
        return source;
    }

    public void setSource(SignalSource source) {
        this.source = source;
    }
}
