package com.stocks.myportfolio.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "portfolio_snapshot")
public class PortfolioSnapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false, unique = true)
    private LocalDate snapshotDate;

    @Column(name = "total_investment", nullable = false)
    private BigDecimal totalInvestment;

    @Column(name = "current_value", nullable = false)
    private BigDecimal currentValue;

    @Column(name = "total_pnl", nullable = false)
    private BigDecimal totalPnL;

    @Column(name = "total_pnl_percentage", nullable = false)
    private BigDecimal totalPnLPercentage;

    @Column(name = "holding_count")
    private Integer holdingCount;

    @Column(name = "top_gainer")
    private String topGainer;

    @Column(name = "top_loser")
    private String topLoser;

    public PortfolioSnapshot() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public BigDecimal getTotalInvestment() {
        return totalInvestment;
    }

    public void setTotalInvestment(BigDecimal totalInvestment) {
        this.totalInvestment = totalInvestment;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getTotalPnL() {
        return totalPnL;
    }

    public void setTotalPnL(BigDecimal totalPnL) {
        this.totalPnL = totalPnL;
    }

    public BigDecimal getTotalPnLPercentage() {
        return totalPnLPercentage;
    }

    public void setTotalPnLPercentage(BigDecimal totalPnLPercentage) {
        this.totalPnLPercentage = totalPnLPercentage;
    }

    public Integer getHoldingCount() {
        return holdingCount;
    }

    public void setHoldingCount(Integer holdingCount) {
        this.holdingCount = holdingCount;
    }

    public String getTopGainer() {
        return topGainer;
    }

    public void setTopGainer(String topGainer) {
        this.topGainer = topGainer;
    }

    public String getTopLoser() {
        return topLoser;
    }

    public void setTopLoser(String topLoser) {
        this.topLoser = topLoser;
    }
}
