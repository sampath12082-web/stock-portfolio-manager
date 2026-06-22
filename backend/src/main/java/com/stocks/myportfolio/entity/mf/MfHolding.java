package com.stocks.myportfolio.entity.mf;

import java.math.BigDecimal;

import com.stocks.myportfolio.entity.BaseEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "mf_holding")
public class MfHolding extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutual_fund_id", nullable = false)
    private MutualFund mutualFund;

    @Column(nullable = false)
    private BigDecimal units;

    @Column(name = "average_nav", nullable = false)
    private BigDecimal averageNav;

    @Column(name = "invested_amount", nullable = false)
    private BigDecimal investedAmount;

    @Column(name = "user_id")
    private Long userId;

    public MfHolding() {}

    public Long getId() { return id; }
    public MutualFund getMutualFund() { return mutualFund; }
    public void setMutualFund(MutualFund mutualFund) { this.mutualFund = mutualFund; }
    public BigDecimal getUnits() { return units; }
    public void setUnits(BigDecimal units) { this.units = units; }
    public BigDecimal getAverageNav() { return averageNav; }
    public void setAverageNav(BigDecimal averageNav) { this.averageNav = averageNav; }
    public BigDecimal getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(BigDecimal investedAmount) { this.investedAmount = investedAmount; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
